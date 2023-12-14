
(ns source-code-documentation.print.assemble
    (:require [fruits.uri.api :as uri]
              [fruits.hiccup.api :as hiccup]
              [io.api :as io]
              [fruits.vector.api :as vector]
              [hiccup.page :refer [html5]]
              [fruits.string.api :as string]
              [source-code-documentation.print.js :as print.js]
              [source-code-documentation.print.css :as print.css]
              [source-code-documentation.print.config :as print.config]
              [source-code-documentation.print.utils :as print.utils]
              [asset-compressor.api :as asset-compressor]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-header-description-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Description"]]
        (vector/concat-items [:div {:class [:text--normal :color--basic]}]
                             (vector/gap-items (:additional header-block) [:br]))])

(defn assemble-doc-header-error-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Error"]]
        [:div {:class [:text--normal :color--warning]}
              (str header-block)]])

(defn assemble-doc-header-important-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Important"]]
        (vector/concat-items [:div {:class [:text--normal :color--warning]}]
                             (vector/gap-items (:additional header-block) [:br]))])

(defn assemble-doc-header-info-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Info"]]
        (vector/concat-items [:div {:class [:text--normal :color--blue]}]
                             (vector/gap-items (:additional header-block) [:br]))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-header-separator
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ _]
  [:div {:class :doc-header--separator}])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-header-param-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Param"]
              [:span {}                                    (-> header-block :value)]
              [:span {:class [:text--micro :color--muted]} (-> header-block :meta first)]
              [:span {:class [:text--micro :color--muted]}
                     (case (-> header-block :meta second) "opt" "optional" "req" "required" "required")]]])

(defn assemble-doc-header-return-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Return"]
              [:span {:class [:text--micro :color--muted]} (-> header-block :meta first)]]])

(defn assemble-doc-header-usage-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [_ _ _ _ header-block]
  [:div {:class :doc-header--block}
        [:div {:class :doc-header--block-label}
              [:span {:class [:text--micro :color--muted]} "Usage"]]
        (vector/concat-items [:pre {:class :doc-header--box}]
                             (vector/gap-items (:additional header-block) [:br]))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-header-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ; @param (map) header-block
  ;
  ; @return (hiccup)
  [state options file-data header header-block]
  (case (-> header-block :type)
        :description (assemble-doc-header-description-block state options file-data header header-block)
        :error       (assemble-doc-header-error-block       state options file-data header header-block)
        :example     (assemble-doc-header-usage-block       state options file-data header header-block)
        :important   (assemble-doc-header-important-block   state options file-data header header-block)
        :info        (assemble-doc-header-info-block        state options file-data header header-block)
        :param       (assemble-doc-header-param-block       state options file-data header header-block)
        :return      (assemble-doc-header-return-block      state options file-data header header-block)
        :separator   (assemble-doc-header-separator         state options file-data header header-block)
        :usage       (assemble-doc-header-usage-block       state options file-data header header-block)
                     [:div (str header-block)]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-source-code
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @return (hiccup)
  [_ _ file-data header]
  (let [collapsible-id (-> header :name (str "--source-code") hiccup/value)
        toggle-f       (str "toggleCollapsible('"collapsible-id"')")]
       [:div {:class :collapsible-wrapper :id collapsible-id :data-expanded "false"}
             [:div {:class [:collapsible-button :text--micro] :onClick toggle-f} "Source Code"]
             [:pre {:class :doc-header--box}
                   (-> file-data :source-codes (vector/first-match #(= (:name %) (:name header))) :body)]]))

(defn assemble-doc-header-name
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @return (hiccup)
  [_ _ _ header]
  [:div {:class :doc-header--name}
        (:name header)])

(defn assemble-doc-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (map) header
  ;
  ; @return (hiccup)
  [state options file-data header]
  (let [header-id (-> header :name hiccup/value)]
       [:div {:id header-id :class :doc-header--wrapper}
             (assemble-doc-header-name state options file-data header)
             (letfn [(f0 [%] (assemble-doc-header-block state options file-data header %))]
                    (hiccup/put-with [:div {:class :doc-header--blocks}] (-> header :blocks) f0))
             [:div {:class :doc-header--separator}]
             (assemble-source-code state options file-data header)]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-header-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:div {:id :doc-header-list--wrapper}
        [:span {:id :doc-header-list--title}
               (-> file-data :ns-map :declaration :name)]
        [:span {:class [:text--micro :color--muted]}
               (case (-> file-data :filepath io/filepath->extension)
                     "clj"  "Clojure namespace"
                     "cljc" "Isomorphic namespace"
                     "cljs" "ClojureScript namespace")]
        (letfn [(f0 [%] (assemble-doc-header state options file-data %))]
               (hiccup/put-with [:div {:id :doc-header-list}] (-> file-data :headers) f0))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-symbol-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:div {:id :symbol-list}
        (letfn [(f0 [%] [:a {:class [:button :color--primary] :href (print.utils/symbol-anchor state options file-data %)}
                            (-> % :name)])]
               (let [symbol-names (-> file-data :headers (vector/sort-items-by :name))]
                    (hiccup/put-with [:div {:class :scroll-container}] symbol-names f0)))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-namespace-list-container
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (hiccup)
  [state options file-data extension]
  (if-let [namespace-list (print.utils/filter-namespaces state options extension)]
          (letfn [(f0 [%] [:a {:class [:button :color--primary (if (f1 %) :button--active)] :href (f2 %)} (-> % :ns-map :declaration :name)])
                  (f2 [%] (print.utils/namespace-uri state options % extension))
                  (f1 [%] (and (= extension (-> % :filepath io/filepath->extension))
                               (= file-data (-> %))))]
                 (let [label (case extension "clj" "Clojure namespaces" "cljc" "Isomorphic namespaces" "cljs" "ClojureScript namespaces")]
                      (hiccup/put-with [:div {:class :namespace-list--container} [:span {:class [:text--micro :color--muted]} label]] namespace-list f0)))))

(defn assemble-namespace-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  (letfn [(f0 [%] (-> % :ns-map :declaration :name))
          (f1 [%] (str "/" (string/replace-part % "." "/") ".html"))
          (fx [% extension] (and (= %         (-> file-data :ns :declaration :name))
                                 (= extension (-> file-data :filepath io/filepath->extension))))
          (f3 [%] (-> % (vector/->items f0) vector/abc-items))]
         [:div {:id :namespace-list}
               [:div {:class :scroll-container}
                     (assemble-namespace-list-container state options file-data "clj")
                     (assemble-namespace-list-container state options file-data "cljc")
                     (assemble-namespace-list-container state options file-data "cljs")]]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-top-bar
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ options _]
  [:div {:id :top-bar}
        [:span {:id :top-bar--library-name}                         (-> options :library :name)]
        [:span {:id :top-bar--library-version :class :color--muted} (-> options :library :version)]
        (let [library-website-pretty-url (-> options :library :website uri/pretty-url)
              library-website-valid-url  (-> options :library :website uri/valid-url)]
             [:a {:id :top-bar--library-uri :class :color--primary :href library-website-valid-url}
                 (-> library-website-pretty-url)])])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-page-body
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:body (assemble-namespace-list  state options file-data)
         (assemble-symbol-list     state options file-data)
         (assemble-doc-header-list state options file-data)
         (assemble-top-bar         state options file-data)])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-page-head
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:head [:link   {:rel "stylesheet" :href print.config/FONT-URI}]
         [:style  {:type "text/css"}        (asset-compressor/compress-css print.css/STYLES)]
         [:script {:type "text/javascript"} (asset-compressor/compress-js  print.js/SCRIPTS)]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-page
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (string)
  [state options file-data]
  (html5 {} [:html (-> state (assemble-page-head options file-data))
                   (-> state (assemble-page-body options file-data) hiccup/unparse-class-vectors hiccup/unparse-css)]))

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
  (let [file-data (or (-> state (print.utils/filter-namespaces options "clj")  vector/abc-items first)
                      (-> state (print.utils/filter-namespaces options "cljc") vector/abc-items first)
                      (-> state (print.utils/filter-namespaces options "cljs") vector/abc-items first))
        extension (-> file-data :filepath io/filepath->extension)
        page-uri  (print.utils/namespace-uri state options file-data extension)]
       [:body {:onLoad (str "window.location.href = \"" page-uri "\"")}]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-cover
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ;
  ; @return (string)
  [state options]
  (html5 {} [:html (-> state (assemble-page-head  options {}))
                   (-> state (assemble-cover-body options) hiccup/unparse-class-vectors hiccup/unparse-css)]))
