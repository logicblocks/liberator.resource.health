(ns liberator-hal.health-resource.core-test
  (:require
    [clojure.test :refer :all]

    [halboy.resource :as hal]
    [halboy.json :as hal-json]

    [ring.mock.request :as ring]
    [ring.middleware.keyword-params :as ring-keyword-params]
    [ring.middleware.params :as ring-params]

    [liberator-hal.health-resource.core :as health-resource]))

(def health-route ["/health" :health])

(defn routes [extras]
  [""
   (concat
     [health-route]
     extras)])

(defn dependencies
  ([] (dependencies []))
  ([extra-routes]
   {:routes (routes extra-routes)}))

(defn resource-handler
  ([dependencies] (resource-handler dependencies {}))
  ([dependencies options]
   (-> (health-resource/handler dependencies options)
     ring-keyword-params/wrap-keyword-params
     ring-params/wrap-params)))

(deftest has-status-200
  (let [handler (resource-handler (dependencies))
        request (ring/request :get "/")
        result (handler request)]
    (is (= (:status result) 200))))

(deftest includes-self-link
  (let [handler (resource-handler (dependencies))
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (= (hal/get-href resource :self) "http://localhost/health"))))
