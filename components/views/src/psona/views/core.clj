(ns psona.views.core
  (:require [tech.merero.hiccup-elements.interface.html :as html]
            [tech.merero.hiccup-elements.interface :as el]
            [hiccup.core :as h]
            [hiccup.page :as p]
            [tech.merero.bulma-views.interface.color :as color]
            [ring.util.response :as res]
            [tech.merero.hiccup-components.interface :as hc]
            [tech.merero.bulma-views.interface.navbar :as nav]))


(defn is-app? [el]
  (-> (el/get-config el :id)
      (= "app")))


(defn append-to-app [html el]
  (el/update-body html
                  (el/update-child-by-fn (html/get-body html) is-app? #(el/append % el))))


(defn add-app [html]
  (html/update-body html  (-> (html/get-body html)
                              (el/append [:div {:id "app"}]))))

(defn empty-page []
  (-> (html/html)
      (add-app)
      (html/add-meta-data {:charset "utf-8"})
      (html/add-meta-data {:name "viewport"
                           :content "width=device-width, initial-scale=1"})
      (html/add-script "https://kit.fontawesome.com/eb2034af05.js")
      (html/add-styleseheet "/css/main.css")
      (html/add-script "https://unpkg.com/hyperscript.org@0.9.5")
      (html/add-script "https://unpkg.com/htmx.org@1.7.0")))


(defn basic []
  (-> (empty-page)
      (append-to-app [::navbar [:components/bag [:navbar]]])))


(defn page-response [page bag]
  (as-> page $
    (hc/->hiccup $ bag)
    (h/html $)
    (str (p/doctype :html5) $)
    (res/response $)))


(defn add-menu-item-to-navbar [navbar {:keys [title href]}]
  (nav/add-menu-item navbar (el/a title
                                  :href href)))


(defn- add-menu-items-to-navbar
  [navbar menu-items]
  (reduce add-menu-item-to-navbar navbar menu-items))


(defn navbar [{:keys [menu-items]}]
  (let [navbar (-> (nav/navbar)
                   (color/set-color :primary)
                   (nav/add-brand [:a {:href "/"}
                                   "LYLTY"])
                   (nav/add-menu))]

    (add-menu-items-to-navbar navbar menu-items)))


(defn fragment-response [fragment bag]
  (as-> fragment $
    (hc/->hiccup $ bag)
    (h/html $)
    (res/response $)))


(hc/reg-component
 ::navbar
 navbar)
