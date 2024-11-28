(ns psona.views.interface
  (:require [psona.views.core :as core]))


(defn basic []
  (core/basic))


(defn page-response [page bag]
  (core/page-response page bag))


(defn append-to-app [page el]
  (core/append-to-app page el))


(defn fragment-response [fragment bag]
  (core/fragment-response fragment bag))
