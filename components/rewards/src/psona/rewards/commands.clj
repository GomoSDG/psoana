(ns psona.rewards.commands
  (:require [psona.rewards.core :as core]
            [tech.merero.cqrs.interface :as cqrs]
            [psona.rewards.db :as db]))

#_(ns-unalias *ns* 'db)

(defn provide-free-beverage-feedback [command-result interactions-since-last-reward]
  (update command-result :data assoc
          :beverages-until-reward (core/number-of-beverages-until-reward interactions-since-last-reward)
          :beverages-bought       (core/number-of-beverages-bought-since-last-reward interactions-since-last-reward)))

(defn check-free-beverage-reward [command-result interactions-since-last-reward current-interaction]
  (if (core/qualifies-for-free-beverage? interactions-since-last-reward)
    (update command-result :events conj
            (cqrs/event :rewards/free-beverage-reward-acquired
                        {:customer-id (:customer-id current-interaction)
                         :interaction-id (:interaction-id current-interaction)
                         :interaction-timestamp (:timestamp current-interaction)}))
    command-result))


(defn save-interaction-command! [{:keys [customer-id]} _]
  (let [interaction (core/create-interaction customer-id)
        last-reward-timestamp (or (-> (db/get-last-reward customer-id)
                                      :interaction-timestamp)
                                  1)
        interactions-since-last-reward (-> (db/get-customer-interactions-after-timestamp customer-id last-reward-timestamp)
                                           (conj interaction)
                                           (set))]
    (db/save-interaction interaction)
    (-> (cqrs/command-result)
        (provide-free-beverage-feedback interactions-since-last-reward)
        (check-free-beverage-reward interactions-since-last-reward interaction))))


(defn create-last-reward-projection-action! [{:keys [payload]}]
  (db/save-last-reward payload))


(defn create-new-reward-action! [{:keys [payload]}]
  (let [reward {:customer-id (:customer-id payload)
                :interaction-id (:interaction-id payload)
                :type "free-beverage"
                :redeemed? false
                :id (.toString (java.util.UUID/randomUUID))
                :voucher-id (.toString (java.util.UUID/randomUUID))}]
    (db/save-reward reward)))
