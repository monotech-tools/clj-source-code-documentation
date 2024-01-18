
(ns source-code-documentation.assemble.page
    (:require [asset-compressor.api                     :as asset-compressor]
              [fruits.hiccup.api                        :as hiccup]
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

(defn assemble-content-block-additional-box
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [_ _ content-block]
  (if (-> content-block :additional vector/not-empty?)
      (vector/concat-items [:pre {:class [:content-block--box :text--s]}]
                           (-> content-block :additional (vector/gap-items [:br]) assemble.utils/unparse-entities))))

(defn assemble-content-block-additional-text
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ; @param (keyword or keywords in vector) class
  ;
  ; @return (hiccup)
  [_ _ content-block class]
  (if (-> content-block :additional vector/not-empty?)
      (vector/concat-items [:pre {:class class}]
                           (-> content-block :additional (vector/gap-items [:br]) assemble.utils/parse-links))))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-bug-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} (-> "Bug")]
              [:pre {:class [:text--xs :color--muted]} (-> content-block :meta first)]
              [:pre {:class [:text--xs :color--muted]} (-> content-block :meta second)]]
        [:pre {:class [:text--m :color--default]} (-> content-block :value)]
        (assemble-content-block-additional-text state options content-block [:text--m :color--warning])])

(defn assemble-description-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Description"]]
        (assemble-content-block-additional-text state options content-block [:text--m :color--basic])])

(defn assemble-error-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [_ _ content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Error"]]
        [:pre {:class [:text--m :color--warning]}
              (str content-block)]])

(defn assemble-important-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Important"]]
        (assemble-content-block-additional-text state options content-block [:text--m :color--warning])])

(defn assemble-info-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Info"]]
        (assemble-content-block-additional-text state options content-block [:text--m :color--primary])])

(defn assemble-note-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Note"]]
        (assemble-content-block-additional-text state options content-block [:text--m :color--muted])])

(defn assemble-plain-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        (assemble-content-block-additional-text state options content-block [:text--m :color--default])])

(defn assemble-title-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:color--primary :text--l :text--bold]} (-> content-block :value)]]
        (assemble-content-block-additional-text state options content-block [:text--m :color--default])])

(defn assemble-todo-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Todo"]]
        (assemble-content-block-additional-text state options content-block [:text--m :color--warning])])

(defn assemble-tutorial-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Tutorial"]
              (-> content-block :value)]
        (assemble-content-block-additional-text state options content-block [:text--m :color--default])])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-atom-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]}   (-> "Atom")]
              [:pre {:class [:text-s   :color--default]} (-> content-block :value)]
              [:pre {:class [:text--xs :color--muted]}   (-> content-block :meta first)]
              [:pre {:class [:text--xs :color--muted]}   (-> content-block :meta second)]]
        (assemble-content-block-additional-text state options content-block [:text--s :color--muted])])

(defn assemble-constant-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]}   (-> "Constant")]
              [:pre {:class [:text-s   :color--default]} (-> content-block :value)]
              [:pre {:class [:text--xs :color--muted]}   (-> content-block :meta first)]
              [:pre {:class [:text--xs :color--muted]}   (-> content-block :meta second)]]
        (assemble-content-block-additional-text state options content-block [:text--s :color--muted])])

(defn assemble-param-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]}   (-> "Param")]
              [:pre {:class [:text-s   :color--default]} (-> content-block :value)]
              [:pre {:class [:text--xs :color--muted]}   (-> content-block :meta first)]
              [:pre {:class [:text--xs :color--muted]}   (-> content-block :meta second (case "opt" "optional" "req" "required" "required"))]]
        (assemble-content-block-additional-text state options content-block [:text--s :color--muted])])

(defn assemble-return-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} (-> "Return")]
              [:pre {:class [:text--xs :color--muted]} (-> content-block :meta first)]
              [:pre {:class [:text--xs :color--muted]} (-> content-block :meta second)]]
        (assemble-content-block-additional-text state options content-block [:text--s :color--muted])])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-code-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} (:value content-block)]]
        (assemble-content-block-additional-box state options content-block)])

(defn assemble-preview-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (let [preview-image-uri (assemble.utils/preview-image-uri state options content-block)]
       [:div {:class :content-block}
             [:div {:class :content-block--label}
                   [:pre {:class [:text--xs :color--muted]} "Preview"]]
             [:img {:class :content-block--preview-image :src preview-image-uri}]
             (assemble-content-block-additional-box state options content-block)]))

(defn assemble-usage-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--muted]} "Usage"]]
        (assemble-content-block-additional-box state options content-block)])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-unknown-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [_ _ content-block]
  [:div {:class :content-block}
        [:div {:class :content-block--label}
              [:pre {:class [:text--xs :color--warning]} "Unknown syntax"]]
        [:pre {:class [:text--m :color--muted]} (str content-block)]])

(defn assemble-separator-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [_ _ _]
  [:div {:class :content-block--separator}])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-content-block
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) content-block
  ;
  ; @return (hiccup)
  [state options content-block]
  (case (-> content-block :type)
        :atom        (assemble-atom-content-block        state options content-block)
        :bug         (assemble-bug-content-block         state options content-block)
        :code        (assemble-code-content-block        state options content-block)
        :constant    (assemble-constant-content-block    state options content-block)
        :description (assemble-description-content-block state options content-block)
        :error       (assemble-error-content-block       state options content-block)
        :important   (assemble-important-content-block   state options content-block)
        :info        (assemble-info-content-block        state options content-block)
        :note        (assemble-note-content-block        state options content-block)
        :param       (assemble-param-content-block       state options content-block)
        :plain       (assemble-plain-content-block       state options content-block)
        :preview     (assemble-preview-content-block     state options content-block)
        :return      (assemble-return-content-block      state options content-block)
        :separator   (assemble-separator-content-block   state options content-block)
        :title       (assemble-title-content-block       state options content-block)
        :todo        (assemble-todo-content-block        state options content-block)
        :tutorial    (assemble-tutorial-content-block    state options content-block)
        :usage       (assemble-usage-content-block       state options content-block)
                     (assemble-unknown-content-block     state options content-block)))

(defn assemble-content-blocks
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (maps in vector) content-blocks
  ;
  ; @return (hiccup)
  [state options content-blocks]
  (letfn [(f0 [%] (assemble-content-block state options %))]
         (hiccup/put-with [:div {:class :content-blocks}] content-blocks f0)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-source-code
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [_ _ section]
  (let [collapsible-id (-> section :name (str "--source-code") hiccup/value)
        toggle-f       (str "toggleCollapsible('"collapsible-id"')")]
       (if (-> section :source-code)
           [:div {:class :collapsible-wrapper :id collapsible-id :data-expanded "false"}
                 [:pre {:class [:collapsible-button :text--xs :text--semi-bold] :onClick toggle-f} "Source Code"]
                 (hiccup/parse-newlines [:pre {:class [:text-s :content-block--box]} (-> section :source-code)])])))

(defn assemble-declaration-name
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [_ _ section]
  [:pre {:class [:declaration--name :color--primary :text--bold]}
        (:name section)])

(defn assemble-declaration
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [state options section]
  (let [declaration-id (-> section :name hiccup/value)]
       [:div {:id declaration-id :class :declaration--wrapper}
             [:pre {:class [:text--xs :color--muted]} "Declaration"]
             (assemble-declaration-name        state options           section)
             (assemble-content-blocks          state options (:content section))
             (assemble-separator-content-block state options {})
             (assemble-source-code             state options section)]))

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
  ; (vector/sort-items-by :name)
  (letfn [(f0 [%] (-> state (assemble-declaration options %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))]
         [:div {:id :declarations--wrapper}
               (-> [:div {:id :declarations}]
                   (hiccup/put-with (f1 :def)  f0)
                   (hiccup/put-with (f1 :defn) f0))]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn assemble-tutorial-label
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [_ _ section]
  [:pre {:class [:tutorial--name :color--secondary :text--bold]}
        (:label section)])

(defn assemble-tutorial
  ; @ignore
  ;
  ; @param (maps in vector) state
  ; @param (map) options
  ; @param (map) section
  ;
  ; @return (hiccup)
  [state options section]
  (let [tutorial-id (-> section :name hiccup/value)]
       [:div {:id tutorial-id :class :tutorial--wrapper}
             [:pre {:class [:text--xs :color--muted]} "Tutorial"]
             (assemble-tutorial-label state options           section)
             (assemble-content-blocks state options (:content section))]))

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
  ; (vector/sort-items-by :name)
  (letfn [(f0 [%] (-> state (assemble-tutorial options %)))
          (f1 [%] (-> state (assemble.utils/filter-sections options file-data %)))]
         [:div {:id :tutorials--wrapper}
               (-> [:div {:id :tutorials}]
                   (hiccup/put-with (f1 :tutorial) f0))]))

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
        [:pre {:id :namespace-header--title :class :text--bold}
              (-> file-data :ns-map :declaration :name)]
        [:pre {:class [:text--xs :color--muted]}
              (-> file-data :filepath io/filepath->extension
                  (case "clj" "Clojure namespace" "cljc" "Isomorphic namespace" "cljs" "ClojureScript namespace" "Unknown"))]])

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
          (f2 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--secondary]} (-> % :label)]])]
         (if-let [tutorials (f1 :tutorial)]
                 (-> [:div {:class :secondary-list--container} [:pre {:class [:text--xs :color--muted]} "Tutorials"]]
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
          (f3 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--primary]} (-> % :name)]])]
         (let [defs (f1 :def) defns (f1 :defn)]
              (if (or defs defns)
                  (-> [:div {:class :secondary-list--container} [:pre {:class [:text--xs :color--muted]} "Declarations"]]
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
  ; @note (#3901)
  (letfn [(f0 [%] (-> state (assemble.utils/namespace-uri     options % extension)))
          (f1 [%] (-> state (assemble.utils/filter-namespaces options %)))
          (f2 [%] (-> %     (vector/sort-items-by #(-> % :ns-map :declaration :name))))
          (f3 [%] (and (-> % :filepath io/filepath->extension (= extension)) (= file-data %)))
          (f4 [%] [:a {:href (f0 %)} [:pre {:class [:button :color--primary (if (f3 %) :button--active)]} (-> % :ns-map :declaration :name)]])]
         (if-let [namespaces (f1 extension)]
                 (let [label (case extension "clj" "Clojure namespaces" "cljc" "Isomorphic namespaces" "cljs" "ClojureScript namespaces")]
                      (-> [:div {:class :primary-list--container} [:pre {:class [:text--xs :color--muted]} label]]
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
        [:pre {:id :top-bar--library-name    :class :text--bold}   (-> options :library :name)]
        [:pre {:id :top-bar--library-version :class :color--muted} (-> options :library :version)]
        (let [library-website-pretty-url (-> options :library :website uri/pretty-url)
              library-website-valid-url  (-> options :library :website uri/valid-url)]
             [:a {:id :top-bar--library-uri :href library-website-valid-url}
                 [:pre {:class :color--primary} (-> library-website-pretty-url)]])])

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
        [:a {:id :bottom-bar--credits-link :href "https://github.com/mt-devtools/clj-source-code-documentation"}
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
