
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
  ; @param (map) declaration
  ;
  ; @return (string)
  [_ _ _ declaration]
  (-> declaration :name hiccup/value (string/prepend "#")))

(defn namespace-path
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

(defn namespace-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (string)
  [state {:keys [base-uri] :as options} file-data extension]
  (let [namespace-path (namespace-path state options file-data extension)]
       (str base-uri namespace-path)))

(defn preview-image-uri
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) header-block
  ;
  ; @return (string)
  [_ {:keys [previews-uri]} header-block]
  (let [preview-path (-> header-block :meta first)]
       (str previews-uri "/" preview-path)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn page-print-path
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (string)
  [state {:keys [output-path] :as options} file-data]
  (let [extension      (-> file-data :filepath io/filepath->extension)
        namespace-path (namespace-path state options file-data extension)]
       (str output-path namespace-path)))

(defn cover-print-path
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (string)
  [state {:keys [output-path] :as options}]
  (str output-path "/index.html"))

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
