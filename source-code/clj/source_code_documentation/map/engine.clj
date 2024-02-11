
(ns source-code-documentation.map.engine
    (:require [fruits.regex.api                      :as regex]
              [fruits.vector.api                     :as vector]
              [io.api                                :as io]
              [source-code-documentation.core.config :as core.config]
              [source-code-documentation.map.utils   :as map.utils]
              [source-code-map.api                   :as source-code-map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn map-source-paths
  ; @ignore
  ;
  ; @description
  ; - Maps namespaces from all CLJ, CLJC, and CLJS source files within the given source directories.
  ; - Although the documentation generator creates documentation only for files that match the provided (or default)
  ;   filename pattern, to handle links and redirections, it requires mapping of namespaces in all available source files.
  ;
  ; @param (vector) state
  ; @param (map) options
  ;
  ; @usage
  ; (map-source-paths []
  ;                   {:filename-pattern #"my\_namespace\_a\.clj" :source-paths ["source-code"]})
  ; =>
  ; [{:filepath "source-code/my_namespace_a.clj" :create-documentation? true  :ns-map {...}}
  ;  {:filepath "source-code/my_namespace_b.clj" :create-documentation? false :ns-map {...}}
  ;  {:filepath "source-code/my_namespace_c.clj" :create-documentation? false :ns-map {...}}]
  ;
  ; @return (maps in vector)
  ; [(map) file-data
  ;   {:create-documentation? (boolean)
  ;    :filepath (string)
  ;    :ns-map (map)}]
  [state {:keys [filename-pattern source-paths]}]
  (letfn [(f0 [filepath]    (-> filepath io/filepath->filename (regex/re-match? filename-pattern)))
          (f1 [source-path] (io/search-files source-path core.config/SOURCE-FILENAME-PATTERN))
          (f2 [filepath]    (println "Mapping file:" filepath)
                            {:filepath filepath :ns-map (source-code-map/read-ns-map filepath) :create-documentation? (f0 filepath)})]
         (-> source-paths (vector/->items f1)
                          (vector/flat-items)
                          (vector/->items f2))))
