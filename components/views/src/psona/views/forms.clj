(ns psona.views.forms
  (:require [tech.merero.bulma-views.interface.form :as form]))

(defn text-input [{:keys [id title errors answers hint] :as opt}]
  (let [value (get answers id)
        errors (get errors id)]
    (form/text-input id
                     :placeholder hint
                     :label title
                     :value value
                     :errors errors)))


(defn password-input [{:keys [id title errors answers hint] :as opt}]
  (let [value (get answers id)
        errors (get errors id)]
    (form/password-input id
                         :placeholder hint
                         :label title
                         :value value
                         :errors errors)))

(defn radio-buttons [{:keys [id title errors answers options] :as opt}]
  (let [value (get answers id)
        errors (get errors id)]
    (form/radio-buttons id
                      :label title
                      :options options
                      :errors errors
                      :value value)))


(defn checkbox [{:keys [id title errors answers options] :as opt}]
  (let [value (get answers id)
        errors (get errors id)]
    (form/checkbox id
                   :label title
                   :options options
                   :errors errors
                   :value value)))


(defn select [{:keys [id title errors answers options hint break-out-links add-on-actions]}]
  (let [value (get answers id)
        errors (get errors id)]
    (form/select id
                 :label title
                 :options options
                 :errors errors
                 :add-on-actions add-on-actions
                 :break-out-links break-out-links
                 :value value
                 :hint hint)))
