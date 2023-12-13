
(ns source-code-documentation.print.utils
    (:require [io.api :as io]
              [fruits.vector.api :as vector]
              [fruits.string.api :as string]
              [fruits.hiccup.api :as hiccup]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn symbol-anchor
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @return (string)
  [_ _ _ header]
  (-> header :name hiccup/value (string/prepend "#")))

(defn namespace-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (string)
  [_ _ file-data extension]
  (-> file-data :ns-map :declaration :name (string/replace-part "." "/")
                                           (string/prepend "/" extension "/")
                                           (string/append ".html")))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn filter-namespaces
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (string) extension
  ;
  ; @example
  ; (filter-namespaces [{:filepath "source-code/my_namespace_a.clj"  ...}
  ;                     {:filepath "source-code/my_namespace_a.cljs" ...}]
  ;                    {...}
  ;                    "clj")
  ; =>
  ; [{:filepath "source-code/my_namespace_a.clj" ...}]
  ;
  ; @return (maps in vector)
  [state _ extension]
  (letfn [(f0 [%] (-> % :filepath io/filepath->extension (= extension)))]
         (-> state (vector/keep-items-by f0)
                   (vector/to-nil {:if-empty? true}))))
