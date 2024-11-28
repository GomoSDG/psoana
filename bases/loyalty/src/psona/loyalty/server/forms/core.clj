(ns psona.loyalty.server.forms.core
  (:require [tech.merero.forms.interface :as forms]
            [tech.merero.forms.interface.views :as form.views]
            [psona.views.interface :as views]))

#_(form.views/reg-input-type :type :text
                           :input-fn views/text-input)


(defn forms-response [{:keys [form bag] :as response}]
  (let [form-name (:form-name form)
        fields (forms/get-form-fields form-name)
        answers (get-in bag [form-name :answers])
        errors (when answers (forms/validate-form form-name answers))]

    (if form
      (views/fragment-response
       [:forms/form
        {:method (:method form)
         :answers [:components/bag [form-name :answers]]
         :errors [:components/bag [form-name :errors]]
         :fields fields
         :action (:action form)
         :action-button (:action-button form)
         :htmx (:htmx form)}]

       (assoc-in bag [form-name :errors]
                 errors))
      response)))




(defn wrap-forms [handler]
  (fn
    ([request]
     (forms-response (handler request)))
    ([request respond raise]
     (forms-response (handler request respond raise)))))


(comment
  (forms/get-form-fields :rewards/service-initiation-form)
  (forms-response {#_:form
                   #_{:form-name :rewards/service-initiation-form}
                   :body 1}))
