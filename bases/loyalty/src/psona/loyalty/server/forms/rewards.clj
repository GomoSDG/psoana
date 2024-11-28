(ns psona.loyalty.server.forms.rewards
  (:require [tech.merero.forms.interface :as forms]))


(defn service-initiation-fields []
  [{:id "mobile-number"
    :type :text
    :title "Custmer cellphone number"
    :hint "e.g. 0810000000"}])

(defn service-initiation-validators []
  [{:field "mobile-number"
    :validators [{:type :required}
                 {:type :regex
                  :matches #"^0\d{9}$"
                  :error-message "Must be 10 digit South African number."}]}])


(forms/reg-form
 :rewards/service-initiation-form
 (service-initiation-fields)
 (service-initiation-validators))
