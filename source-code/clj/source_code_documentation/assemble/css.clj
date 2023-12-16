
(ns source-code-documentation.assemble.css)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def STYLES "

/*** Body ***/

body {
  font-family:    Montserrat;
  margin:         0 0 0 480px;
  padding-bottom: 32px;
  padding-left:   48px;
  padding-right:  48px;
  padding-top:    60px }

div {
  box-sizing: border-box }

span {
  display: block }

.scroll-container {
  flex-grow:  1;
  overflow-y: auto }

/*** Body ***/



/*** Buttons, links ***/

a,
a:active,
a:hover,
a:visited {
  display:         block;
  text-decoration: none }

.button {
  font-size:   14px;
  line-height: 24px;
  padding:     0 12px }

.button:hover {
  background-color: #f0f0f0 }

.button--active {
  background-color: #f3f3f3 }

.button--active:hover {
  background-color: #f0f0f0 }

/*** Buttons, links ***/



/*** Text ***/

.text--micro {
  font-size:      10px;
  line-height:    24px;
  text-transform: uppercase }

.text--normal {
  font-size:   16px;
  line-height: 18px }

/*** Text ***/



/*** Colors ***/

.color--basic {
  color: #303030 }

.color--primary {
  color: #0088cc }

.color--muted {
  color: #606060 }

.color--warning {
  color: #883300 }

/*** Colors ***/



/*** Collapsible ***/

.collapsible-wrapper {
  display:        flex;
  flex-direction: column }

.collapsible-button {
  cursor:      pointer;
  font-weight: 500;
  position:    relative;
  user-select: none;
  width:       fit-content }

.collapsible-button:hover {
  color: #888 }

.collapsible-button::after {
  align-items:     center;
  content:         '▼';
  display:         flex;
  font-size:       8px;
  justify-content: center;
  position:        absolute;
  height:          24px;
  right:           -24px;
  top:             0;
  width:           24px }

.collapsible-wrapper[data-expanded=\"true\"] .collapsible-button::after {
  content: '▲' }

.collapsible-wrapper .doc-header--box {
  display: none }

.collapsible-wrapper[data-expanded=\"true\"] .doc-header--box {
  display: block }

/*** Collapsible ***/



/*** Top bar ***/

#top-bar {
  background-color: #ffffff;
  border-bottom:    1px solid #e0e0e0;
  display:          flex;
  gap:              6px;
  height:           60px;
  left:             0;
  padding-left:     18px;
  padding-top:      18px;
  position:         fixed;
  top:              0;
  width:            100% }

#top-bar--library-uri {
  font-size:   12px;
  line-height: 24px;
  position:    absolute;
  right:       18px;
  top:         18px }

#top-bar--library-uri:hover {
  opacity: .5 }

#top-bar--library-name {
  font-size:      18px;
  font-weight:    600;
  line-height:    24px;
  text-transform: uppercase }

#top-bar--author-name {
  font-size: 12px }

#top-bar--library-version {
  font-size: 12px }

/*** Top bar ***/



/*** Primary list, secondary list ***/

#primary-list {
  border-right:   1px solid #e0e0e0;
  display:        flex;
  flex-direction: column;
  height:         calc(100vh - 60px);
  left:           0;
  padding:        12px 0;
  position:       fixed;
  top:            60px;
  width:          240px }

#secondary-list {
  border-right:   1px solid #e0e0e0;
  display:        flex;
  flex-direction: column;
  height:         calc(100vh - 60px);
  left:           240px;
  padding:        12px 0;
  position:       fixed;
  top:            60px;
  width:          240px }

#primary-list .text--micro,
#secondary-list .text--micro {
  padding-left: 12px }

.primary-list--container,
.secondary-list--container {
  margin-bottom: 12px }

/*** Primary list, secondary list ***/



/*** Cover ***/

#credits {
  align-items:    center;
  display:        flex;
  flex-direction: column;
  margin-left:    240px;
  padding-top:    120px }

/*** Cover ***/



/*** Namespace header ***/

#namespace-header--title {
  font-size:   18px;
  font-weight: 600;
  line-height: 24px;
  margin-top:  24px }

/*** Namespace header ***/



/*** Declarations ***/

#declarations {
  display:        flex;
  flex-direction: column }

/*** Declarations ***/



/*** Declaration ***/

.declaration--wrapper {
  padding-top: 64px }

.declaration--name {
  border-bottom:  1px solid #e0e0e0;
  color:          #558899;
  font-size:      16px;
  font-weight:    600;
  line-height:    18px;
  padding-bottom: 8px;
  margin-bottom:  12px }

/*** Declaration ***/



/*** Tutorial ***/

.tutorial--wrapper {
  padding-top: 64px }

.tutorial--name {
  border-bottom:  1px solid #e0e0e0;
  color:          #665599;
  font-size:      16px;
  font-weight:    600;
  line-height:    18px;
  padding-bottom: 8px;
  margin-bottom:  12px }

/*** Tutorial ***/



/*** Doc blocks ***/

.doc-blocks {
  display: flex;
  flex-direction: column }

.doc-block--separator {
  height: 12px }

.doc-block--label {
  display:     flex;
  gap:         4px;
  line-height: 24px }

.doc-block--box {
  background-color: #fafafa;
  color:            #333;
  font-size:        12px;
  line-height:      16px;
  margin:           0;
  padding:          12px 8px }

.doc-block--preview-image {
  border:     1px solid #dedede;
  display:    block;
  max-height: 360px;
  max-width:  640px;
  min-height: 36px;
  min-width:  48px }

/*** Doc blocks ***/



/*** Library credits ***/

#library-credits {
  background-color: white;
  border-right:     1px solid #e0e0e0;
  border-top:       1px solid #e0e0e0;
  bottom:           0;
  left:             0;
  position:         fixed;
  width:            480px }

#library-credits--link {
  color:          #55bb66;
  font-size:      12px;
  letter-spacing: .8px;
  line-height:    48px;
  text-align:     center }

#library-credits--link:hover {
  background-color: #f0f0f0
}

/*** Library credits ***/")
