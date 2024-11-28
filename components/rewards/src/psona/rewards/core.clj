(ns psona.rewards.core)

(defn create-interaction [customer-id]
  {:interaction-id (.toString (java.util.UUID/randomUUID))
   :customer-id customer-id
   :timestamp (.getTime (java.util.Date.))})

(def beverages-until-free-beverage-reward 9)

(defn number-of-beverages-bought-since-last-reward [interactions-since-last-reward]
  (count interactions-since-last-reward))


(defn number-of-beverages-until-reward [interactions-since-last-reward]
  (- beverages-until-free-beverage-reward
     (number-of-beverages-bought-since-last-reward interactions-since-last-reward)))


(defn beverage-sale-rewards-info [interactions-since-last-reward]
  {:number-of-beverages-bought (number-of-beverages-bought-since-last-reward interactions-since-last-reward)
   :number-of-beverages-to-reward (number-of-beverages-until-reward interactions-since-last-reward)})


(defn qualifies-for-free-beverage? [interactions-since-last-reward]
  (>= (count interactions-since-last-reward)
      beverages-until-free-beverage-reward))
