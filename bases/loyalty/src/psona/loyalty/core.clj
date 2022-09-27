(ns psona.loyalty.core
  (:require [tech.merero.db.interface :as db]
            [tech.merero.cqrs.interface :as cqrs]
            [tech.merero.pubsub.interface :as pubsub]
            [taoensso.timbre :as log]))


(db/reg-tables
 [(db/table {:table-id :interactions
             :table-name :interactions
             :fields {:customer-id :string
                      :timestamp :number
                      :id :string}
             :sort-key :timestamp
             :primary-key [:customer-id :string]})
  (db/table {:table-id :last-reward-interaction
             :table-name :last-reward-interaction
             :fields {:interaction-id :string
                      :interaction-timestamp :number
                      :customer-id :string}
             :primary-key [:customer-id :string]})
  (db/table {:table-id :rewards
             :table-name :rewards
             :fields {:interaction-id :string
                      :customer-id :string
                      :type :string
                      :voucher-number :string
                      :redeemed? :boolean
                      :id :string}
             :primary-key [:customer-id :string]
             :sort-key :id})])

(comment


  (pubsub/subscribe-all!)

  (pubsub/stop-all-subscriptions!)
  )


;; Interactions

(def beverages-until-free-beverage-reward 9)


(defn create-interaction [customer-id]
  {:interaction-id (.toString (java.util.UUID/randomUUID))
   :customer-id customer-id
   :timestamp (.getTime (java.util.Date.))})


(defn save-interaction [interaction]
  (db/save-item :interactions interaction))


(defn get-customer-interactions [customer-id]
  (db/query :interactions [:customer-id customer-id]))

(defn get-customer-interactions-after-timestamp [customer-id timestamp]
  (db/query :interactions [:customer-id customer-id]
            :sort-key [:timestamp :gt timestamp]))


(defn save-last-reward [last-reward]
  (db/save-item :last-reward-interaction last-reward))


(defn get-last-reward [customer-id]
  (db/get-item :last-reward-interaction [:customer-id customer-id]))


(defn save-reward [reward]
  (db/save-item :rewards reward))


(defn number-of-beverages-bought-since-last-reward [interactions-since-last-reward]
  (count interactions-since-last-reward))


(defn number-of-beverages-until-reward [interactions-since-last-reward]
  (- beverages-until-free-beverage-reward
     (number-of-beverages-bought-since-last-reward interactions-since-last-reward)))


(defn provide-free-beverage-feedback [command-result interactions-since-last-reward]
  (update command-result :data assoc
          :beverages-until-reward (number-of-beverages-until-reward interactions-since-last-reward)
          :beverages-bought       (number-of-beverages-bought-since-last-reward interactions-since-last-reward)))


(defn qualifies-for-free-beverage? [interactions-since-last-reward]
  (>= (count interactions-since-last-reward)
      beverages-until-free-beverage-reward))


(defn check-free-beverage-reward [command-result interactions-since-last-reward current-interaction]
  (if (qualifies-for-free-beverage? interactions-since-last-reward)
    (update command-result :events conj
            (cqrs/event :rewards/free-beverage-reward-acquired
                        {:customer-id (:customer-id current-interaction)
                         :interaction-id (:interaction-id current-interaction)
                         :interaction-timestamp (:timestamp current-interaction)}))
    command-result))


(defn save-interaction-command! [{:keys [customer-id]} _]
  (let [interaction (create-interaction customer-id)
        last-reward-timestamp (or (-> (get-last-reward customer-id)
                                      :interaction-timestamp)
                                  1)
        interactions-since-last-reward (-> (get-customer-interactions-after-timestamp customer-id last-reward-timestamp)
                                           (conj interaction)
                                           (set))]
    (save-interaction interaction)
    (-> (cqrs/command-result)
        (provide-free-beverage-feedback interactions-since-last-reward)
        (check-free-beverage-reward interactions-since-last-reward interaction))))


(defn handle-free-beverage-reward-acquired [{:keys [payload]}]
  (save-last-reward payload))


(defn create-new-reward-action! [{:keys [payload]}]
  ())


(defn buy-bev [customer-id]
  (cqrs/handle-command! {} :rewards/save-intereaction {:customer-id customer-id}))

(comment
  (cqrs/reg-command! {:handler save-interaction-command!
                      :type :rewards/save-intereaction})

  (cqrs/reg-event-handler
   :rewards/free-beverage-reward-acquired
   :rewards/create-last-reward-projection
   handle-free-beverage-reward-acquired)

  (cqrs/reg-event-handler
   :rewards/free-beverage-reward-acquired
   :rewards/save-new-reward
   create-new-reward-action!)


  (pubsub/stop-all-subscriptions!)
  (pubsub/subscribe-all!)

  (buy-bev "0813600442")
  (get-customer-interactions "0813600442")
  (count (get-customer-interactions-after-timestamp "0813600442" 1664201391644N))
  )
