(ns psona.loyalty.core
  (:gen-class)
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


;; Interactions

(defn buy-bev [customer-id]
  (cqrs/handle-command! {} :rewards/save-interaction {:customer-id customer-id}))

(comment
  (pubsub/stop-all-subscriptions!)
  (pubsub/subscribe-all!)

  (buy-bev "0813600442")
  )
