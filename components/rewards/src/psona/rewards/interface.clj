(ns psona.rewards.interface
  (:require [psona.rewards.queries :as queries]
            [psona.rewards.db :as db]))

#_(ns-unalias *ns* 'db)


(defn beverage-sale-rewards-info [customer-id]
  (queries/beverage-sale-rewards-info customer-id))


(defn get-rewards-for-customer [customer-id]
  (db/get-rewards-for-customer customer-id))
