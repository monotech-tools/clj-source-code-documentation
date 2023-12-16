
(ns source-code-documentation.assemble.cover
    (:require [fruits.hiccup.api                      :as hiccup]
              [fruits.uri.api                         :as uri]
              [fruits.vector.api                      :as vector]
              [hiccup.page                            :refer [html5]]
              [io.api                                 :as io]
              [source-code-documentation.assemble.page :as assemble.page]
              [source-code-documentation.assemble.utils :as assemble.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-credits
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (hiccup)
  [_ options]
  (let [author-website-pretty-url  (-> options :author  :website uri/pretty-url)
        author-website-valid-url   (-> options :author  :website uri/valid-url)
        library-website-pretty-url (-> options :library :website uri/pretty-url)
        library-website-valid-url  (-> options :library :website uri/valid-url)]
       [:div {:id :credits}
             [:table {}
                     (if (-> options :library :name)    [:tr [:td "Name:"]    [:td [:div (-> options :library :name)]]])
                     (if (-> options :library :website) [:tr [:td "Website:"] [:td [:a {:class :color--primary :href library-website-valid-url} library-website-pretty-url]]])
                     (if (-> options :library :version) [:tr [:td "Version:"] [:td [:div (-> options :library :version)]]])
                     (if (-> options :author  :name)    [:tr [:td "Author:"]  [:td [:div (-> options :author :name)]]])
                     (if (-> options :author  :website) [:tr [:td "Website:"] [:td [:a {:class :color--primary :href author-website-valid-url} author-website-pretty-url]]])]]))

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
  (let [file-data (or (-> state (assemble.utils/filter-namespaces options "clj")  vector/abc-items first)
                      (-> state (assemble.utils/filter-namespaces options "cljc") vector/abc-items first)
                      (-> state (assemble.utils/filter-namespaces options "cljs") vector/abc-items first))
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
