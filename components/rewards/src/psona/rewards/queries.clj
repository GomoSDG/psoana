(ns psona.rewards.queries
  (:require [psona.rewards.db :as db]
            [psona.rewards.core :as core]))


(defn beverage-sale-rewards-info [customer-id]
  (let [last-reward (db/get-last-reward customer-id)
        interactions-since-last-reward (db/get-customer-interactions-after-timestamp
                                        customer-id
                                        (or (:interaction-timestamp last-reward) 1))]
    (core/beverage-sale-rewards-info interactions-since-last-reward)))


(comment
  (beverage-sale-rewards-info "0183600442")
  (db/get-rewards-for-customer "0813600442"))
