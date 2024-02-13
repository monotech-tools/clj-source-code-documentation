
(ns source-code-documentation.assemble.page
    (:require [asset-compressor.api                     :as asset-compressor]
              [fruits.hiccup.api                        :as hiccup]
              [fruits.random.api                        :as random]
              [fruits.string.api                        :as string]
              [fruits.uri.api                           :as uri]
              [fruits.vector.api                        :as vector]
              [hiccup.page                              :refer [html5]]
              [io.api                                   :as io]
              [source-code-documentation.assemble.css   :as assemble.css]
              [source-code-documentation.assemble.js    :as assemble.js]
              [source-code-documentation.assemble.utils :as assemble.utils]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-snippet-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) snippet
  ; @param (keyword) snippet-id
  ; @param (map) snippet-props
  ; {:collapsible? (boolean)(opt)
  ;  :label-class (keyword or keywords in vector)(opt)
  ;  :marker-class (keyword or keywords in vector)(opt)
  ;  :meta-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [_ _ snippet snippet-id {:keys [collapsible? label-class marker-class meta-class]}]
  (let [toggle-f (str "toggleCollapsible('"snippet-id"')")]
       [(if collapsible? :button :div)
        (if collapsible? {:class [:snippet--header] :onClick toggle-f}
                         {:class [:snippet--header]})
        (if-let [marker (-> snippet :marker)]      [:pre {:class marker-class} (-> marker assemble.utils/normalize-marker)])
        (if-let [meta   (-> snippet :meta first)]  [:pre {:class meta-class}   (-> meta)])
        (if-let [meta   (-> snippet :meta second)] [:pre {:class meta-class}   (-> meta)])
        (if-let [label  (-> snippet :label)]       [:pre {:class label-class}  (-> label)])]))

(defn assemble-snippet-preview
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) snippet
  ; @param (keyword) snippet-id
  ; @param (map) snippet-props
  ; {:image-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [state options snippet _ {:keys [image-class]}]
  (if (and (-> snippet :meta first string?)
           (-> snippet :meta first io/filepath->image?))
      (let [preview-image-uri (assemble.utils/preview-image-uri state options snippet)]
           [:div {:class image-class}
                 [:img {:class [:snippet--preview-image] :src preview-image-uri}]])))

(defn assemble-snippet-text
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) snippet
  ; @param (keyword) snippet-id
  ; @param (map) snippet-props
  ; {:text-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [_ _ snippet _ {:keys [text-class]}]
  (if (-> snippet :text vector/not-empty?)
      [:div {:class [:snippet--text]}
            (vector/concat-items [:pre {:class text-class}]
                                 (-> snippet :text assemble.utils/gap-rows assemble.utils/parse-links assemble.utils/unparse-html))]))

(defn assemble-snippet-wrapper
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) snippet
  ; @param (keyword) snippet-id
  ; @param (map) snippet-props
  ; {:collapsed? (boolean)(opt)
  ;  :collapsible? (boolean)(opt)
  ;  :image-class (keyword or keywords in vector)(opt)
  ;  :label-class (keyword or keywords in vector)(opt)
  ;  :marker-class (keyword or keywords in vector)(opt)
  ;  :meta-class (keyword or keywords in vector)(opt)
  ;  :text-class (keyword or keywords in vector)(opt)}
  ;
  ; @return (hiccup)
  [state options snippet snippet-id snippet-props]
  (let [data-collapsed   (-> snippet-props :collapsed?   boolean str)
        data-collapsible (-> snippet-props :collapsible? boolean str)]
       [:div {:class [:snippet] :id snippet-id :data-collapsed data-collapsed :data-collapsible data-collapsible}
             (assemble-snippet-header  state options snippet snippet-id snippet-props)
             (assemble-snippet-preview state options snippet snippet-id snippet-props)
             (assemble-snippet-text    state options snippet snippet-id snippet-props)]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-snippet
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) snippet
  ;
  ; @return (hiccup)
  [state options snippet]
  (let [snippet-id    (random/generate-string)
        snippet-props (assemble.utils/config-snippet state options snippet)]
       (assemble-snippet-wrapper state options snippet snippet-id snippet-props)))

(defn assemble-snippets
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ; {:content (maps in vector)
  ;  ...}
  ;
  ; @return (hiccup)
  [state options {:keys [content]}]
  (letfn [(f0 [%] (assemble-snippet state options %))]
         (hiccup/put-with [:div {:class [:snippets]}] content f0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-section-header
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ; {:label (string)
  ;  :type (keyword)
  ;  ...}
  ; @param (map) header-props
  ; {:label-class (keyword or keywords in vector)(opt)
  ;  ...}
  ;
  ; @return (hiccup)
  [_ _ section {:keys [label-class]}]
  [:div {:class [:section--header]}
        [:pre {:class [:color--soft-grey :text--xxs :text--uppercase]} (:type  section)]
        [:pre {:class label-class}                                     (:label section)]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-declaration
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [state options section]
  (let [section        (->> section (assemble.utils/append-declaration-source-code state options))
        section        (->> section (assemble.utils/sort-section-content           state options))
        declaration-id (->> section :name hiccup/value)]
       [:div {:id declaration-id :class [:section]}
             (assemble-section-header state options section {:label-class [:color--hard-blue :text--xxl :text--bold]})
             (assemble-snippets       state options section)]))

(defn assemble-declarations
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  ; (vector/sort-items-by [...] :name)
  (letfn [(f0 [%] (-> state (assemble-declaration options %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))]
         (-> [:div {:class [:sections]}]
             (hiccup/put-with (f1 :def)  f0)
             (hiccup/put-with (f1 :defn) f0))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-tutorial
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [state options section]
  (let [section     (->> section (assemble.utils/sort-section-content state options))
        tutorial-id (->> section :name hiccup/value)]
       [:div {:id tutorial-id :class [:section]}
             (assemble-section-header state options section {:label-class [:color--hard-purple :text--xxl :text--wrap :text--bold]})
             (assemble-snippets       state options section)]))

(defn assemble-tutorials
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  ; (vector/sort-items-by [...] :name)
  (letfn [(f0 [%] (-> state (assemble-tutorial options %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))]
         (-> [:div {:class [:sections]}]
             (hiccup/put-with (f1 :tutorial) f0))))

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
  [state options file-data]
  (let [namespace-name (-> file-data :ns-map :declaration :name)
        namespace-type (-> file-data :filepath io/filepath->extension assemble.utils/extension->namespace-type)]
       (assemble-section-header state options {:label namespace-name
                                               :type  namespace-type}
                                              {:label-class [:color--black :text--xxl :text--wrap :text--bold]})))

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
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/symbol-anchor   options file-data %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))
          (f2 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--hard-purple :text--m]} (-> % :label)]])]
         (if-let [tutorials (f1 :tutorial)]
                 (-> [:div {:class [:secondary-list--container]} [:pre {:class [:label :color--soft-grey :text--xxs :text--uppercase]} "Tutorials"]]
                     (hiccup/put-with tutorials f2)))))

(defn assemble-declaration-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/symbol-anchor   options file-data %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))
          (f2 [%] (-> %     (vector/sort-items-by :name)))
          (f3 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--hard-blue :text--m]} (-> % :name)]])]
         (let [defs (f1 :def) defns (f1 :defn)]
              (if (or defs defns)
                  (-> [:div {:class [:secondary-list--container]} [:pre {:class [:label :color--soft-grey :text--xxs :text--uppercase]} "Declarations"]]
                      (hiccup/put-with (f2 defs)  f3)
                      (hiccup/put-with (f2 defns) f3))))))

(defn assemble-secondary-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  [:div {:id :secondary-list}
        [:div {:class [:scroll-y]}
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
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/namespace-uri     options % extension)))
          (f1 [%] (-> state (assemble.utils/filter-namespaces options %)))
          (f2 [%] (-> %     (assemble.utils/sort-namespaces   options)))
          (f3 [%] (and (-> % :filepath io/filepath->extension (= extension)) (= file-data %)))
          (f4 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--hard-blue :text--m (if (f3 %) :button--active)]} (-> % :ns-map :declaration :name)]])]
         (if-let [namespaces (f1 extension)]
                 (let [namespaces-type (assemble.utils/extension->namespaces-type extension)]
                      (-> [:div {:class [:primary-list--container]} [:pre {:class [:label :color--soft-grey :text--xxs :text--uppercase]} namespaces-type]]
                          (hiccup/put-with (f2 namespaces) f4))))))

(defn assemble-primary-list
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [state options file-data]
  ; @note (#3901)
  ; - The namespace list and declaration list items are alphabetically ordered (to make searching items easy).
  ; - Tutorial list items are displayed in their written order as they were placed in files (their written order could be important!).
  ; - The actual declaration and tutorial sections are displayed in their written order as they were placed in files (their written order could be important!).
  [:div {:id :primary-list}
        [:div {:class [:scroll-y]}
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
        [:pre {:id :top-bar--library-name    :class [:text--xxl :text--bold]}     (-> options :library :name)]
        [:pre {:id :top-bar--library-version :class [:color--soft-grey :text--s]} (-> options :library :version)]
        (let [library-website-pretty-url (-> options :library :website uri/pretty-url)
              library-website-valid-url  (-> options :library :website uri/valid-url)]
             [:a {:id :top-bar--library-uri :class [:text--s] :href library-website-valid-url}
                 [:pre {:class [:color--hard-blue]} (-> library-website-pretty-url)]])])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-bottom-bar
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:div {:id :bottom-bar}
        [:a {:id :bottom-bar--credits-link :class [:color--soft-purple :text--s] :href "https://github.com/mt-devtools/clj-source-code-documentation"}
            [:pre "github.com/mt-devtools/clj-source-code-documentation"]]])

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
         (assemble-bottom-bar       state options file-data)])

(defn assemble-page-head
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) file-data
  ;
  ; @return (hiccup)
  [_ _ _]
  [:head [:style  {:type "text/css"}        (asset-compressor/compress-css assemble.css/STYLES)]
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
