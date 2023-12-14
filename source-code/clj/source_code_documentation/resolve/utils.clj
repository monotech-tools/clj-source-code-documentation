
(ns source-code-documentation.resolve.utils
    (:require [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn create-pointer
  ; @ignore
  ;
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @example
  ; (create-pointer {:ns-map {:declaration {:name "my-namespace"}}}
  ;                 {:name "my-function" :blocks [...]})
  ; =>
  ; :my-namespace/my-function
  ;
  ; @return (namespaced keyword)
  [file-data header]
  (keyword (-> file-data :ns-map :declaration :name)
           (-> header :name)))

(defn update-trace
  ; @ignore
  ;
  ; @param (namespaced keywords in vector) trace
  ; @param (namespaced keyword) pointer
  ;
  ; @example
  ; (update-trace [:my-namespace/my-function] :another-namespace/another-function)
  ; =>
  ; [:my-namespace/my-function :another-namespace/another-function]
  ;
  ; @return (namespaced keywords in vector)
  [trace pointer]
  (if (vector/contains-item? trace pointer)
      (let [trace (conj trace pointer)] (throw (Exception. (str "Circular pointer error.\n" trace))))
      (let [trace (conj trace pointer)] (-> trace))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------
