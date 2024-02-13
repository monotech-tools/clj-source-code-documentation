
(ns source-code-documentation.assemble.css)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @constant (string)
(def STYLES "
/*** Body ***/

body {
  margin:  12px 0 0 560px;
  padding: 60px 48px }

button {
  background-color: transparent;
  border:           none;
  cursor:           pointer;
  padding:          0;
  user-select:      none }

button:hover {
  opacity: .5 }

div {
  box-sizing: border-box }

pre {
  display:        block;
  margin:         0;
  letter-spacing: .8px }
/*** Body ***/



/*** Scroll ***/

.scroll-x {
  overflow-x: auto }

.scroll-y {
  flex-grow:  1;
  overflow-y: auto }

/*** Scroll ***/



/*** Buttons, links ***/

a,
a:active,
a:hover,
a:visited {
  display:         block;
  text-decoration: none }

a.inline-link {
  display: inline-block }

a.inline-link:hover {
  text-decoration: underline }

.button {
  /* Shrinks buttons to make them display more text in narrow sidebars. */
  letter-spacing: .5px;

  white-space: inherit }

.button:hover {
  background-color: #f0f0f0 }

.button--active {
  background-color: #f3f3f3 }

.button--active:hover {
  background-color: #f0f0f0 }

/*** Buttons, links ***/



/*** Text ***/

.text--xxs {
  font-size:      10px;
  line-height:    18px }

.text--xs {
  font-size:      11px;
  line-height:    18px }

.text--s {
  font-size:   12px;
  line-height: 18px }

.text--m {
  font-size:   13px;
  line-height: 18px }

.text--l {
  font-size:   14px;
  line-height: 18px }

.text--xl {
  font-size:   16px;
  line-height: 24px }

.text--xxl {
  font-size:   18px;
  line-height: 24px }

.text--semi-bold {
  font-weight: 500 }

.text--bold {
  font-weight: 600 }

.text--uppercase {
  text-transform: uppercase }

.text--boxed {
  background-color: #fafafa;
  padding:          12px 8px }

.text--hidden {
  display: none }

.text--wrap {
 -white-space: normal;
  /* Wraps lines if necessary, but doesn't collapse multiple white-spaces. */
  white-space: pre-wrap }

/*** Text ***/



/*** Image ***/

.image--boxed {
  background-color: #fafafa;
  padding:          12px 8px }

/*** Image ***/



/*** Colors ***/

.color--black {
  color: #000000 }

.color--hard-grey {
  color: #404040 }

.color--soft-grey {
  color: #808080 }

.color--hard-blue {
  color: #0088cc }

.color--soft-blue {
  color: #55aabb }

.color--hard-purple {
  color: #8800cc }

.color--soft-purple {
  color: #aa55bb }

.color--hard-red {
  color: #cc00aa }

/*** Colors ***/



/*** Collapsible ***/

[data-collapsed=\"true\"] .snippet--text {
  display: none }

[data-collapsible=\"true\"] .snippet--header::after {
  align-items:     center;
  content:         '▼';
  display:         flex;
  font-size:       8px;
  justify-content: center;
  height:          18px;
  width:           18px
}

[data-collapsible=\"true\"][data-collapsed=\"false\"] .snippet--header::after {
  content: '▲' }

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
  position:         fixed;
  top:              0;
  width:            100% }

#top-bar--library-uri {
  padding:  21px 18px;
  position: absolute;
  right:    0;
  top:      0 }

#top-bar--library-uri:hover {
  background-color: #f0f0f0 }

#top-bar--library-name {
  padding:        18px 0;
  text-transform: uppercase }

#top-bar--library-version {
  margin-top: 12px }

/*** Top bar ***/



/*** Bottom bar ***/

#bottom-bar {
  background-color: white;
  border-top:       1px solid #e0e0e0;
  bottom:           0;
  display:          flex;
  justify-content:  right;
  left:             0;
  position:         fixed;
  width:            100% }

#bottom-bar--credits-link {
  padding: 12px 18px }

#bottom-bar--credits-link:hover {
  background-color: #f0f0f0 }

/*** Bottom bar ***/



/*** Primary list, secondary list ***/

#primary-list {
  background-color: #fff;
  border-right:     1px solid #e0e0e0;
  display:          flex;
  flex-direction:   column;
  height:           calc(100vh - 60px);
  left:             0;
  padding:          12px 0 54px 0;
  position:         fixed;
  top:              60px;
  width:            280px }

#secondary-list {
  background-color: #fff;
  border-right:     1px solid #e0e0e0;
  display:          flex;
  flex-direction:   column;
  height:           calc(100vh - 60px);
  left:             280px;
  padding:          12px 0 54px 0;
  position:         fixed;
  top:              60px;
  width:            280px }

#primary-list,
#secondary-list {
  /*  Relatively positioned elements can overflow the sidebars in case of horizontal scrolling! */
  z-index: 9999 }

#primary-list .text--xs,
#secondary-list .text--xs {
  padding-left: 12px }

.primary-list--container,
.secondary-list--container {
  margin-bottom: 12px }
  
#primary-list .button,
#secondary-list .button {
  padding: 3px 12px }

#primary-list .label,
#secondary-list .label {
  padding: 0 12px }

/*** Primary list, secondary list ***/



/*** Cover ***/

/*** Cover ***/



/*** Sections ***/

.section {
  padding: 72px 0 }

.section--header {
  border-bottom:  1px solid #e0e0e0;
  padding-bottom: 8px;
  margin-bottom:  12px }

/*** Sections ***/



/*** Snippets ***/

.snippets {
  display:        flex;
  flex-direction: column;
  gap:            24px }

.snippet--header {
  display: flex;
  gap:     4px }

.snippet--preview-image {
  border:     1px solid #dedede;
  display:    block;
  max-height: 480px;
  max-width:  640px;
  min-height: 48px;
  min-width:  64px }

/*** Snippets ***/")
