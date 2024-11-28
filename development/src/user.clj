(ns user
  (:require [psona.loyalty.server.core :as loyalty]
            [org.httpkit.server :as http]
            [ring.middleware.reload :as reload]
            [tech.merero.db.interface :as db]
            [tech.merero.pubsub.interface :as pubsub]
            [taoensso.timbre :as log]))


(defonce loyalty-server (atom nil))


(defn start-loyalty []
  (reset! loyalty-server (http/run-server (reload/wrap-reload #'loyalty/app {:dirs ["bases/loyalty/src/"]})
                                          {:port 4090})))


(defn stop-loyalty []
  (@loyalty-server))


(defn reload-loyalty []
  (stop-loyalty)
  (start-loyalty))


(defn decoder [val]
  (cheshire.core/decode val keyword))


(defn try-parse-int [val]
  (try
    (Integer/parseInt val)
    (catch Exception e
      nil)))


(comment
  (try-parse-int "")
  ;; property-prices
  (db/reg-tables
   [(db/table {:table-id :property24-sold-prices
               :table-name :property24-sold-prices
               :fields {:id :string}
               :primary-key [:id :string]})])

  (pubsub/deftopics
    [(pubsub/topic {:id :r-entities
                    :name :r-entities
                    :encoder :pr-str})])

  (defn save-property [property-price]
    (db/save-item :property24-sold-prices (assoc property-price
                                                 :id (-> (java.util.UUID/randomUUID)
                                                         (.toString)))))

  (def prices (volatile! #{}))

  (defn prln [v]
    (binding [*out* *out*]
      (println v)))

  (prln "Hello")

  (defn save-property-price [property-price]
    (log/info property-price)
    (vswap! prices conj (-> property-price
                            (update :price try-parse-int)
                            (update :year try-parse-int))))

  (count @prices)

  (-> (sort-by (juxt :price :year) @prices)
      (reverse))

  (count (filter #(or (nil? (:price %))
                      (nil? (:year %)))
                 @prices))

  (spit "../property24-prices-3.edn" @prices)

  (first @prices)


  (pubsub/defsubscriptions
    [(pubsub/subscription
      {:topic-name :r-entities
       :subscription-name "cleaned-prices"
       :decoder decoder
       :concurrency 10
       :handler save-property-price})])

  (pubsub/subscribe-all!)

  (pubsub/stop-all-subscriptions!)
  )
