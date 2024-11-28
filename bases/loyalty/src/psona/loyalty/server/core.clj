(ns psona.loyalty.server.core
  (:require [reitit.ring :as ring]
            [hiccup.core :as h]
            [reitit.middleware :as middleware]
            [ring.middleware.params :as params]
            [psona.rewards.interface :as rewards]
            [tech.merero.cqrs.interface :as cqrs]
            [tech.merero.middleware.interface.roles :as roles]
            [psona.views.interface :as views]
            [tech.merero.bulma-views.interface.form :as form]
            [tech.merero.bulma-views.interface.elements :as els]
            [tech.merero.hiccup-elements.interface :as el]
            [psona.loyalty.server.forms.core :as forms]
            [tech.merero.forms.interface :as f]
            [ring.util.response :as res]
            [psona.rewards.interface.commands]))

#_(ns-unalias *ns* 'forms)


(defn index [_]
  (-> (views/basic)
      (views/append-to-app
       [:section.section
        [:div.container.is-max-desktop
         [:h1.title "Register customer interaction"]
         [:h1.subtitle "On this screen you will enter the mobile number of the client that you are serving.
                       If the client has an account with us, you can the register their interaction with us."]
         [:div.columns.is-centered.my-2
          [:div.column.is-half
           [:div {:hx-get "/api/service-initiation"
                  :hx-trigger "load"}]]]]])
      (views/page-response {})))


(defn dashboard [{:keys [params]}]
  (let [customer-id (get params "cellphone-number")
        beverage-info (rewards/beverage-sale-rewards-info customer-id)
        rewards       (rewards/get-rewards-for-customer customer-id)]
    {:body (h/html (list [:h1 "Hello " customer-id]
                         [:table
                          [:tbody
                           [:tr
                            [:th "Beverages bought"]
                            [:td (:number-of-beverages-bought beverage-info)]]
                           [:tr
                            [:th "Beverages to free beverage"]
                            [:td (:number-of-beverages-to-reward beverage-info)]]]]
                         [:form {:action "/buy-beverage"
                                 :method "post"}
                          [:input {:type "hidden"
                                   :name "cellphone-number"
                                   :value customer-id}]
                          [:br]
                          [:input {:type "submit"
                                   :value "Buy beverage"}]]
                         [:h2 "Rewards (" (count rewards) ")"]
                         [:ul
                          (map (fn [{:keys [type voucher-id]}]
                                 [:li type " - " voucher-id])
                               rewards)]))
     :headers {"Content-Type" "text/html"}}))


(defn buy-beverage [{:keys [params] :as req}]
  (cqrs/handle-command! {} :rewards/save-interaction {:customer-id (get params "cellphone-number")})
  (dashboard req))


(defn service-initiation-form [req]
  {:form
   {:form-name :rewards/service-initiation-form
    :htmx {:method "post"
           :url "/api/service-initiation"}
    :action-button (-> (form/field
                        (form/control
                         (-> (els/button "Cancel"
                                         :type "button")
                             (el/update-config-with-fn #(assoc % :_ "on click send cancelRosterEdit to body"))))
                        (form/control
                         (els/button "Next"
                                     :color "is-primary")))
                       (el/add-classes "is-grouped"))}
   :bag {}})


(defn submit-service-initiation-form [{:keys [params] :as req}]
  (let [customer-id (get params "mobile-number")
        beverage-info (when (not-empty customer-id) (rewards/beverage-sale-rewards-info customer-id))
        rewards       (when (not-empty customer-id) (rewards/get-rewards-for-customer customer-id))]
    (if (f/validate-form :rewards/service-initiation-form params)
      {:form
       {:form-name :rewards/service-initiation-form
        :htmx {:method "post"
               :url "/api/service-initiation"}
        :action-button (-> (form/field
                            (form/control
                             (-> (els/button "Cancel"
                                             :type "button")
                                 (el/update-config-with-fn #(assoc % :_ "on click send cancelRosterEdit to body"))))
                            (form/control
                             (els/button "Next"
                                         :color "is-primary")))
                           (el/add-classes "is-grouped"))}
       :bag {:answers params}}

      (views/fragment-response
       [:div
        [:table.table
         [:tbody
          [:tr
           [:th "Beverages bought"]
           [:td (:number-of-beverages-bought beverage-info)]]
          [:tr
           [:th "Beverages to free beverage"]
           [:td (:number-of-beverages-to-reward beverage-info)]]
          [:tr
           [:th "Total available rewards"]
           [:td (count rewards)]]]]
        [:button.button.is-primary
         {:hx-post (str "/api/interaction/" customer-id)}
         "Add Interaction"]]
       {}))))


(defn register-interaction [{:keys [path-params] :as req}]
  (println path-params)
  (clojure.pprint/pprint req)
  (cqrs/handle-command! {} :rewards/save-interaction {:customer-id (:customer-id path-params)})

  {:headers {"HX-Redirect" "/"}})


(defn router []
  (ring/router
   ["" {:middleware [[:wrap-params] #_[:wrap-roles {:required-roles #{"What!?"}}]]}
    ["/" {:get index}]
    ["/customer-beverages" {:post dashboard}]
    ["/buy-beverage" {:post buy-beverage}]
    ["/api" {}
     ["/service-initiation" {:middleware [[:wrap-forms]]
                             :get service-initiation-form
                             :post submit-service-initiation-form}]
     ["/interaction/:customer-id" {:post register-interaction}]]]
   {::middleware/registry {:wrap-params params/wrap-params
                           :wrap-roles  roles/wrap-roles
                           :wrap-forms  forms/wrap-forms}}))


(def app (ring/ring-handler
          (router)
          (ring/routes
           (ring/create-resource-handler {:root "loyalty/assets"
                                          :path "/"})
           (ring/redirect-trailing-slash-handler))))
