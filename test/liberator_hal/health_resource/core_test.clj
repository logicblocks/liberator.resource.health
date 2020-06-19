(ns liberator-hal.health-resource.core-test
  (:require
   [clojure.test :refer :all]

   [halboy.resource :as hal]
   [halboy.json :as hal-json]

   [pathological.files :as files]

   [ring.mock.request :as ring]
   [ring.middleware.keyword-params :as ring-keyword-params]
   [ring.middleware.params :as ring-params]

   [liberator-hal.health-resource.core :as health-resource]))

(def discovery-route ["/" :discovery])
(def health-route ["/health" :health])

(defn routes [extras]
  [""
   (concat
     [discovery-route
      health-route]
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

(deftest includes-discovery-link
  (let [handler (resource-handler (dependencies))
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (= (hal/get-href resource :discovery) "http://localhost/"))))

(deftest does-not-include-a-version-attribute-by-default
  (let [handler (resource-handler (dependencies))
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (not (contains? (hal/properties resource) :version)))))

(deftest includes-version-attribute-when-version-file-path-provided
  (let [version "1.2.0+fc9c14c"
        version-file-path (files/write-lines
                            (files/create-temp-file "version" ".txt")
                            [version])
        handler (resource-handler (dependencies)
                  {:version-file-path (str version-file-path)})
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (files/delete version-file-path)

    (is (= (hal/get-property resource :version) version))))

(deftest includes-version-of-missing-when-version-file-not-readable
  (let [version-file-path "/some/missing/version/file"
        handler (resource-handler (dependencies)
                  {:version-file-path (str version-file-path)})
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (= (hal/get-property resource :version) "missing"))))

(deftest does-not-include-dependencies-by-default
  (let [handler (resource-handler (dependencies))
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (not (contains? (hal/properties resource) :dependencies)))))

(deftest includes-health-check-result-for-single-dependency
  (let [dependency-check
        (health-resource/dependency-check
          :thing (fn [_] {:healthy true}))
        handler (resource-handler (dependencies)
                  {:dependency-checks [dependency-check]})
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (= (hal/get-in-properties resource
             [:dependencies :thing])
          {:healthy true}))))

(deftest includes-health-check-result-for-many-dependencies
  (let [dependency-1-check
        (health-resource/dependency-check
          :thing-1 (fn [_] {:healthy true}))
        dependency-2-check
        (health-resource/dependency-check
          :thing-2 (fn [_] {:healthy false}))
        handler (resource-handler (dependencies)
                  {:dependency-checks [dependency-1-check
                                       dependency-2-check]})
        request (ring/request :get "http://localhost/health")
        result (handler request)
        resource (hal-json/json->resource (:body result))]
    (is (= (hal/get-in-properties resource
             [:dependencies :thing1])
          {:healthy true}))
    (is (= (hal/get-in-properties resource
             [:dependencies :thing2])
          {:healthy false}))))
