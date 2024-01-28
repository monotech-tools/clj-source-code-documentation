
(ns source-code-documentation.assemble.cover
    (:require [fruits.hiccup.api                        :as hiccup]
              [fruits.uri.api                           :as uri]
              [fruits.vector.api                        :as vector]
              [hiccup.page                              :refer [html5]]
              [io.api                                   :as io]
              [source-code-documentation.assemble.page  :as assemble.page]
              [source-code-documentation.assemble.utils :as assemble.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-cover-body
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (hiccup)
  [state options]
  ; Instead of displaying a cover page, it redirects to the first namespace in the namespace tree.
  (let [file-data (or (-> state (assemble.utils/filter-namespaces options "clj")
                                (assemble.utils/sort-namespaces   options) first)
                      (-> state (assemble.utils/filter-namespaces options "cljc")
                                (assemble.utils/sort-namespaces   options) first)
                      (-> state (assemble.utils/filter-namespaces options "cljs")
                                (assemble.utils/sort-namespaces   options) first))
        extension (-> file-data :filepath io/filepath->extension)
        page-uri  (assemble.utils/namespace-uri state options file-data extension)]
       [:body {:onLoad (str "window.location.href = \"" page-uri "\"")}]))

(defn assemble-cover-head
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (hiccup)
  [state options]
  (assemble.page/assemble-page-head state options {}))

(defn assemble-cover
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (string)
  [state options]
  (html5 {} [:html (-> state (assemble-cover-head options))
                   (-> state (assemble-cover-body options) hiccup/unparse-class-vectors hiccup/unparse-css)]))
