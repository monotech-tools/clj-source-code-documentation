
(ns source-code-documentation.assemble.page
    (:require [asset-compressor.api                   :as asset-compressor]
              [fruits.hiccup.api                      :as hiccup]
              [fruits.uri.api                         :as uri]
              [fruits.vector.api                      :as vector]
              [hiccup.page                            :refer [html5]]
              [io.api                                 :as io]
              [source-code-documentation.assemble.config :as assemble.config]
              [source-code-documentation.assemble.css    :as assemble.css]
              [source-code-documentation.assemble.js     :as assemble.js]
              [source-code-documentation.assemble.utils  :as assemble.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-description-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Description"]]
        (vector/concat-items [:div {:class [:text--normal :color--basic]}]
                             (vector/gap-items (:additional doc-block) [:br]))])

(defn assemble-doc-error-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Error"]]
        [:div {:class [:text--normal :color--warning]}
              (str doc-block)]])

(defn assemble-doc-important-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Important"]]
        (vector/concat-items [:div {:class [:text--normal :color--warning]}]
                             (vector/gap-items (:additional doc-block) [:br]))])

(defn assemble-doc-info-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Info"]]
        (vector/concat-items [:div {:class [:text--normal :color--blue]}]
                             (vector/gap-items (:additional doc-block) [:br]))])

(defn assemble-doc-plain-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        (vector/concat-items [:div {:class [:text--normal :color--blue]}]
                             (vector/gap-items (:additional doc-block) [:br]))])

(defn assemble-doc-tutorial-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Tutorial"]]
        (-> doc-block :value)
        (vector/concat-items [:div {:class [:text--normal :color--blue]}]
                             (vector/gap-items (:additional doc-block) [:br]))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-param-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Param"]
              [:span {}                                    (-> doc-block :value)]
              [:span {:class [:text--micro :color--muted]} (-> doc-block :meta first)]
              [:span {:class [:text--micro :color--muted]}
                     (case (-> doc-block :meta second) "opt" "optional" "req" "required" "required")]]])

(defn assemble-doc-return-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Return"]
              [:span {:class [:text--micro :color--muted]} (-> doc-block :meta first)]]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-preview-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [state options doc-block]
  (let [preview-image-uri (assemble.utils/preview-image-uri state options doc-block)]
       [:div {:class :doc-block}
             [:div {:class :doc-block--label}
                   [:span {:class [:text--micro :color--muted]} "Preview"]]
             [:img {:class :doc-block--preview-image :src preview-image-uri}]
             (vector/concat-items [:pre {:class :doc-block--box}]
                                  (vector/gap-items (:additional doc-block) [:br]))]))

(defn assemble-doc-usage-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ doc-block]
  [:div {:class :doc-block}
        [:div {:class :doc-block--label}
              [:span {:class [:text--micro :color--muted]} "Usage"]]
        (vector/concat-items [:pre {:class :doc-block--box}]
                             (vector/gap-items (:additional doc-block) [:br]))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-separator-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [_ _ _]
  [:div {:class :doc-block--separator}])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-doc-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) doc-block
  ;
  ; @return (hiccup)
  [state options doc-block]
  (case (-> doc-block :type)
        :description (assemble-doc-description-block state options doc-block)
        :error       (assemble-doc-error-block       state options doc-block)
        :important   (assemble-doc-important-block   state options doc-block)
        :info        (assemble-doc-info-block        state options doc-block)
        :param       (assemble-doc-param-block       state options doc-block)
        :plain       (assemble-doc-plain-block       state options doc-block)
        :preview     (assemble-doc-preview-block     state options doc-block)
        :return      (assemble-doc-return-block      state options doc-block)
        :separator   (assemble-doc-separator-block   state options doc-block)
        :tutorial    (assemble-doc-tutorial-block    state options doc-block)
        :usage       (assemble-doc-usage-block       state options doc-block)
                     [:div (str doc-block)]))

(defn assemble-doc-blocks
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (maps in vector) doc-blocks
  ;
  ; @return (hiccup)
  [state options doc-blocks]
  (letfn [(f0 [%] (assemble-doc-block state options %))]
         (hiccup/put-with [:div {:class :doc-blocks}] doc-blocks f0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-source-code
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) declaration
  ;
  ; @return (hiccup)
  [_ _ declaration]
  (let [collapsible-id (-> declaration :name (str "--source-code") hiccup/value)
        toggle-f       (str "toggleCollapsible('"collapsible-id"')")]
       [:div {:class :collapsible-wrapper :id collapsible-id :data-expanded "false"}
             [:div {:class [:collapsible-button :text--micro] :onClick toggle-f} "Source Code"]
             [:pre {:class :doc-block--box}
                   (-> declaration :body)]]))

(defn assemble-declaration-name
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) declaration
  ;
  ; @return (hiccup)
  [_ _ declaration]
  [:div {:class :declaration--name}
        (:name declaration)])

(defn assemble-declaration
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) declaration
  ;
  ; @return (hiccup)
  [state options declaration]
  (let [declaration-id (-> declaration :name hiccup/value)]
       [:div {:id declaration-id :class :declaration--wrapper}
             [:span {:class [:text--micro :color--muted]} "Declaration"]
             (assemble-declaration-name    state options          declaration)
             (assemble-doc-blocks          state options (:header declaration))
             (assemble-doc-separator-block state options {})
             (assemble-source-code         state options declaration)]))

(defn assemble-declarations
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:div {:id :declarations--wrapper}
        (letfn [(f0 [%] (assemble-declaration state options %))]
               (hiccup/put-with [:div {:id :declarations}] (-> file-data :declarations) f0))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-tutorial-name
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) tutorial
  ;
  ; @return (hiccup)
  [_ _ tutorial]
  [:div {:class :tutorial--name}
        (:name tutorial)])

(defn assemble-tutorial
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) tutorial
  ;
  ; @return (hiccup)
  [state options tutorial]
  (let [tutorial-id (-> tutorial :name hiccup/value)]
       [:div {:id tutorial-id :class :tutorial--wrapper}
             [:span {:class [:text--micro :color--muted]} "Tutorial"]
             (assemble-tutorial-name state options           tutorial)
             (assemble-doc-blocks    state options (:content tutorial))]))

(defn assemble-tutorials
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:div {:id :tutorials--wrapper}
        (letfn [(f0 [%] (assemble-tutorial state options %))]
               (hiccup/put-with [:div {:id :tutorials}] (-> file-data :tutorials) f0))])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-namespace-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ file-data]
  [:div {:id :namespace-header}
        [:span {:id :namespace-header--title}
               (-> file-data :ns-map :declaration :name)]
        [:span {:class [:text--micro :color--muted]}
               (case (-> file-data :filepath io/filepath->extension)
                     "clj"  "Clojure namespace"
                     "cljc" "Isomorphic namespace"
                     "cljs" "ClojureScript namespace")]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-tutorial-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  (letfn [(f0 [%] (assemble.utils/symbol-anchor state options file-data %))
          (f1 [%] [:a {:class [:button :color--primary] :href (f0 %)} (-> % :name)])]
         (let [label "Tutorials" tutorials (-> file-data :tutorials)]
              (hiccup/put-with [:div {:class :secondary-list--container} [:span {:class [:text--micro :color--muted]} label]] tutorials f1))))

(defn assemble-declaration-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  (letfn [(f0 [%] (assemble.utils/symbol-anchor state options file-data %))
          (f1 [%] [:a {:class [:button :color--primary] :href (f0 %)} (-> % :name)])]
         (let [label "Declarations" declarations (-> file-data :declarations (vector/sort-items-by :name))]
              (hiccup/put-with [:div {:class :secondary-list--container} [:span {:class [:text--micro :color--muted]} label]] declarations f1))))

(defn assemble-secondary-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:div {:id :secondary-list}
        [:div {:class :scroll-container}
              (assemble-tutorial-list    state options file-data)
              (assemble-declaration-list state options file-data)]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-namespace-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ; @param (string) extension
  ;
  ; @return (hiccup)
  [state options file-data extension]
  (if-let [namespaces (assemble.utils/filter-namespaces state options extension)]
          (letfn [(f0 [%] [:a {:class [:button :color--primary (if (f2 %) :button--active)] :href (f1 %)} (-> % :ns-map :declaration :name)])
                  (f1 [%] (assemble.utils/namespace-uri state options % extension))
                  (f2 [%] (and (= extension (-> % :filepath io/filepath->extension))
                               (= file-data (-> %))))]
                 (let [label (case extension "clj" "Clojure namespaces" "cljc" "Isomorphic namespaces" "cljs" "ClojureScript namespaces")]
                      (hiccup/put-with [:div {:class :primary-list--container} [:span {:class [:text--micro :color--muted]} label]] namespaces f0)))))

(defn assemble-primary-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:div {:id :primary-list}
        [:div {:class :scroll-container}
              (assemble-namespace-list state options file-data "clj")
              (assemble-namespace-list state options file-data "cljc")
              (assemble-namespace-list state options file-data "cljs")]])

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

(defn assemble-library-credits
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:div {:id :library-credits}
        [:a {:id :library-credits--link :href "https://github.com/mt-devtools/clj-source-code-documentation"}
            "[mt-devtools/clj-source-code-documentation]"]])

(defn assemble-page-body
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  [:body (assemble-primary-list     state options file-data)
         (assemble-secondary-list   state options file-data)
         (assemble-namespace-header state options file-data)
         (assemble-tutorials        state options file-data)
         (assemble-declarations     state options file-data)
         (assemble-top-bar          state options file-data)
         (assemble-library-credits  state options file-data)])

(defn assemble-page-head
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:head [:link   {:rel "stylesheet" :href assemble.config/FONT-URI}]
         [:style  {:type "text/css"}        (asset-compressor/compress-css assemble.css/STYLES)]
         [:script {:type "text/javascript"} (asset-compressor/compress-js  assemble.js/SCRIPTS)]])

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
