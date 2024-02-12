
(ns source-code-documentation.map.engine
    (:require [fruits.regex.api                      :as regex]
              [fruits.vector.api                     :as vector]
              [io.api                                :as io]
              [source-code-documentation.core.config :as core.config]
              [source-code-documentation.map.utils   :as map.utils]
              [source-code-map.api                   :as source-code-map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn map-source-file
  ; @ignore
  ;
  ; @param (vector) state
  ; @param (map) options
  ; {:filename-pattern (regex-pattern)
  ;  ...}
  ; @param (string) filepath
  ;
  ; @usage
  ; (map-source-file [] {...} "source-code/my_namespace_a.clj")
  ; =>
  ; {:create-documentation? true
  ;  :filepath              "source-code/my_namespace_a.clj"
  ;  :ns-map                {...}}
  ;
  ; @return (map)
  ; {:create-documentation? (boolean)
  ;  :filepath (string)
  ;  :ns-map (map)}
  [_ {:keys [filename-pattern]} filepath]
  (letfn [(f0 [filepath] (-> filepath io/filepath->filename (regex/re-match? filename-pattern)))]
         (println "Mapping file:" filepath)
         {:create-documentation? (f0 filepath)
          :filepath              (-> filepath)
          :ns-map                (-> filepath source-code-map/read-ns-map)}))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn search-source-files
  ; @ignore
  ;
  ; @param (vector) state
  ; @param (map) options
  ; {:filename-pattern (regex-pattern)
  ;  :trace-redirections? (boolean)(opt)
  ;  ...}
  ; @param (string) source-path
  ;
  ; @usage
  ; (search-source-files [] {...} "source-code")
  ; =>
  ; ["source-code/my_namespace_a.clj"
  ;  "source-code/my_namespace_b.clj"
  ;  "source-code/my_namespace_c.clj"]
  ;
  ; @return (strings in vector)
  [_ {:keys [filename-pattern trace-redirections?]} source-path]
  (if trace-redirections? (io/search-files source-path core.config/SOURCE-FILENAME-PATTERN)
                          (io/search-files source-path filename-pattern)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn map-source-paths
  ; @ignore
  ;
  ; @param (vector) state
  ; @param (map) options
  ; {:source-paths (strings in vector)
  ;  ...}
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
  ;    :ns-map (map)}
  ;  ...]
  [state {:keys [source-paths] :as options}]
  (letfn [(f0 [source-path] (search-source-files state options source-path))
          (f1 [filepath]    (map-source-file     state options filepath))]
         (-> source-paths (vector/->items f0)
                          (vector/flat-items)
                          (vector/->items f1))))
