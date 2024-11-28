(ns psona.rewards.db
  (:require [tech.merero.db.interface :as db]))

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


(defn get-rewards-for-customer [customer-id]
  (db/query :rewards [:customer-id customer-id]
            :filter {:redeemed? false}))


(comment
  (get-customer-interactions "0813600442")
  (count (get-customer-interactions-after-timestamp "0813600442" 1664201391644N)))
