/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * A [ListView] example where the data comes from a custom ListAdapter, the [OnItemClickListener]
 * of the [ListView] is set to a lambda which calls the [onListItemClick] override which was used
 * when this was a `ListActivity`. This method "toggles" the view between two states: collapsed to
 * a title, and expanded to a title and text for that title.
 */
class List6 : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`. Then we set our content view to our layout file `R.layout.list_6`, and initialize
     * our [ListView] variable `val list` by finding the view with ID `R.id.list`. Next we set the
     * list adapter of `list` to a new instance of [SpeechListAdapter], and set its [OnItemClickListener]
     * to a lambda which calls our method [onListItemClick] (thereby replicating the behavior when
     * this was a `ListActivity`).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_6)
        val list = findViewById<ListView>(R.id.list)

        // Use our own list adapter
        list.adapter = SpeechListAdapter(this)
        list.setOnItemClickListener { parent, view, position, id ->
            onListItemClick(parent as ListView, view, position, id)
        }
    }

    /**
     * This method will be called when an item in the list is selected. We Get the `ListAdapter`
     * associated with this activity's `ListView`, cast it to an [SpeechListAdapter] and
     * call its `toggle` method with our [position] parameter.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Suppress("UNUSED_PARAMETER")
    fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        (l.adapter as SpeechListAdapter).toggle(position)
    }

    /**
     * A sample `ListAdapter` that presents content from arrays of speeches and text.
     */
    private inner class SpeechListAdapter
    /**
     * Our constructor, we just save our [Context] parameter in our field [mContext].
     *
     * @param mContext [Context] to use to construct Views
     */
    (
            /**
             * Remember our context so we can use it when constructing views.
             */
            private val mContext: Context

    ) : BaseAdapter() {
        /**
         * How many items are in the data set represented by this Adapter. The number of items in
         * the list is determined by the number of speeches in our array, so we just return the
         * length of our [String] array field [mTitles].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mTitles.size
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an array, just returning the index is sufficient to get at the data. If we
         * were using a more complex data structure, we would return whatever object represents one
         * row in the list.
         *
         * @param position Position of the item whose data we want within the adapter's data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return position
        }

        /**
         * Get the row id associated with the specified position in the list. Use the array index as
         * a unique id.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a [View] that displays the data at the specified position in the data set. First we
         * declare [SpeechView] variable `val sv`. If [convertView] is null, we create a new instance
         * of [SpeechView] for `sv` constructed using the title from index [position] in [mTitles],
         * dialog from index [position] in [mDialogue], and expanded state from index [position] in
         * [mExpanded]. If [convertView] is not null we cast it to a [SpeechView] to set `sv`, call
         * the method `sv.setTitle` to set the title to the text in index [position] of [mTitles],
         * call the method `sv.setDialogue` to set the dialog to the text in index [position] of
         * [mDialogue], and call the method `sv.setExpanded` to set the expanded state to the value
         * of the contents of the [Boolean] at index [position] in [mExpanded]. In either case we
         * return `sv` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old [View] to reuse, if possible.
         * @param parent      The parent that this [View] will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val sv: SpeechView
            if (convertView == null) {
                sv = SpeechView(mContext, mTitles[position], mDialogue[position], mExpanded[position])
            } else {
                sv = convertView as SpeechView
                sv.setTitle(mTitles[position])
                sv.setDialogue(mDialogue[position])
                sv.setExpanded(mExpanded[position])
            }
            return sv
        }

        /**
         * Toggles the state of the [SpeechView] at position [position] in our array. First we
         * toggle the value of the contents at index [position] in [mExpanded], then we call the
         * method `notifyDataSetChanged` to notify the attached observers that the underlying data
         * has been changed and any [View] reflecting the data set should refresh itself.
         *
         * @param position The position of the item within the adapter's data set we are to toggle.
         */
        fun toggle(position: Int) {
            mExpanded[position] = !mExpanded[position]
            notifyDataSetChanged()
        }

        /**
         * Our data, part 1.
         */
        private val mTitles = arrayOf(
                "Henry IV (1)",
                "Henry V",
                "Henry VIII",
                "Richard II",
                "Richard III",
                "Merchant of Venice",
                "Othello",
                "King Lear"
        )

        /**
         * Our data, part 2.
         */
        private val mDialogue = arrayOf(
                "So shaken as we are, so wan with care," +
                        "Find we a time for frighted peace to pant," +
                        "And breathe short-winded accents of new broils" +
                        "To be commenced in strands afar remote." +
                        "No more the thirsty entrance of this soil" +
                        "Shall daub her lips with her own children's blood;" +
                        "Nor more shall trenching war channel her fields," +
                        "Nor bruise her flowerets with the armed hoofs" +
                        "Of hostile paces: those opposed eyes," +
                        "Which, like the meteors of a troubled heaven," +
                        "All of one nature, of one substance bred," +
                        "Did lately meet in the intestine shock" +
                        "And furious close of civil butchery" +
                        "Shall now, in mutual well-beseeming ranks," +
                        "March all one way and be no more opposed" +
                        "Against acquaintance, kindred and allies:" +
                        "The edge of war, like an ill-sheathed knife," +
                        "No more shall cut his master. Therefore, friends," +
                        "As far as to the sepulchre of Christ," +
                        "Whose soldier now, under whose blessed cross" +
                        "We are impressed and engaged to fight," +
                        "Forthwith a power of English shall we levy;" +
                        "Whose arms were moulded in their mothers' womb" +
                        "To chase these pagans in those holy fields" +
                        "Over whose acres walk'd those blessed feet" +
                        "Which fourteen hundred years ago were nail'd" +
                        "For our advantage on the bitter cross." +
                        "But this our purpose now is twelve month old," +
                        "And bootless 'tis to tell you we will go:" +
                        "Therefore we meet not now. Then let me hear" +
                        "Of you, my gentle cousin Westmoreland," +
                        "What yesternight our council did decree" +
                        "In forwarding this dear expedience.",
                "Hear him but reason in divinity," +
                        "And all-admiring with an inward wish" +
                        "You would desire the king were made a prelate:" +
                        "Hear him debate of commonwealth affairs," +
                        "You would say it hath been all in all his study:" +
                        "List his discourse of war, and you shall hear" +
                        "A fearful battle render'd you in music:" +
                        "Turn him to any cause of policy," +
                        "The Gordian knot of it he will unloose," +
                        "Familiar as his garter: that, when he speaks," +
                        "The air, a charter'd libertine, is still," +
                        "And the mute wonder lurketh in men's ears," +
                        "To steal his sweet and honey'd sentences;" +
                        "So that the art and practic part of life" +
                        "Must be the mistress to this theoric:" +
                        "Which is a wonder how his grace should glean it," +
                        "Since his addiction was to courses vain," +
                        "His companies unletter'd, rude and shallow," +
                        "His hours fill'd up with riots, banquets, sports," +
                        "And never noted in him any study," +
                        "Any retirement, any sequestration" +
                        "From open haunts and popularity.",
                "I come no more to make you laugh: things now," +
                        "That bear a weighty and a serious brow," +
                        "Sad, high, and working, full of state and woe," +
                        "Such noble scenes as draw the eye to flow," +
                        "We now present. Those that can pity, here" +
                        "May, if they think it well, let fall a tear;" +
                        "The subject will deserve it. Such as give" +
                        "Their money out of hope they may believe," +
                        "May here find truth too. Those that come to see" +
                        "Only a show or two, and so agree" +
                        "The play may pass, if they be still and willing," +
                        "I'll undertake may see away their shilling" +
                        "Richly in two short hours. Only they" +
                        "That come to hear a merry bawdy play," +
                        "A noise of targets, or to see a fellow" +
                        "In a long motley coat guarded with yellow," +
                        "Will be deceived; for, gentle hearers, know," +
                        "To rank our chosen truth with such a show" +
                        "As fool and fight is, beside forfeiting" +
                        "Our own brains, and the opinion that we bring," +
                        "To make that only true we now intend," +
                        "Will leave us never an understanding friend." +
                        "Therefore, for goodness' sake, and as you are known" +
                        "The first and happiest hearers of the town," +
                        "Be sad, as we would make ye: think ye see" +
                        "The very persons of our noble story" +
                        "As they were living; think you see them great," +
                        "And follow'd with the general throng and sweat" +
                        "Of thousand friends; then in a moment, see" +
                        "How soon this mightiness meets misery:" +
                        "And, if you can be merry then, I'll say" +
                        "A man may weep upon his wedding-day.",
                "First, heaven be the record to my speech!" +
                        "In the devotion of a subject's love," +
                        "Tendering the precious safety of my prince," +
                        "And free from other misbegotten hate," +
                        "Come I appellant to this princely presence." +
                        "Now, Thomas Mowbray, do I turn to thee," +
                        "And mark my greeting well; for what I speak" +
                        "My body shall make good upon this earth," +
                        "Or my divine soul answer it in heaven." +
                        "Thou art a traitor and a miscreant," +
                        "Too good to be so and too bad to live," +
                        "Since the more fair and crystal is the sky," +
                        "The uglier seem the clouds that in it fly." +
                        "Once more, the more to aggravate the note," +
                        "With a foul traitor's name stuff I thy throat;" +
                        "And wish, so please my sovereign, ere I move," +
                        "What my tongue speaks my right drawn sword may prove.",
                "Now is the winter of our discontent" +
                        "Made glorious summer by this sun of York;" +
                        "And all the clouds that lour'd upon our house" +
                        "In the deep bosom of the ocean buried." +
                        "Now are our brows bound with victorious wreaths;" +
                        "Our bruised arms hung up for monuments;" +
                        "Our stern alarums changed to merry meetings," +
                        "Our dreadful marches to delightful measures." +
                        "Grim-visaged war hath smooth'd his wrinkled front;" +
                        "And now, instead of mounting barded steeds" +
                        "To fright the souls of fearful adversaries," +
                        "He capers nimbly in a lady's chamber" +
                        "To the lascivious pleasing of a lute." +
                        "But I, that am not shaped for sportive tricks," +
                        "Nor made to court an amorous looking-glass;" +
                        "I, that am rudely stamp'd, and want love's majesty" +
                        "To strut before a wanton ambling nymph;" +
                        "I, that am curtail'd of this fair proportion," +
                        "Cheated of feature by dissembling nature," +
                        "Deformed, unfinish'd, sent before my time" +
                        "Into this breathing world, scarce half made up," +
                        "And that so lamely and unfashionable" +
                        "That dogs bark at me as I halt by them;" +
                        "Why, I, in this weak piping time of peace," +
                        "Have no delight to pass away the time," +
                        "Unless to spy my shadow in the sun" +
                        "And descant on mine own deformity:" +
                        "And therefore, since I cannot prove a lover," +
                        "To entertain these fair well-spoken days," +
                        "I am determined to prove a villain" +
                        "And hate the idle pleasures of these days." +
                        "Plots have I laid, inductions dangerous," +
                        "By drunken prophecies, libels and dreams," +
                        "To set my brother Clarence and the king" +
                        "In deadly hate the one against the other:" +
                        "And if King Edward be as true and just" +
                        "As I am subtle, false and treacherous," +
                        "This day should Clarence closely be mew'd up," +
                        "About a prophecy, which says that 'G'" +
                        "Of Edward's heirs the murderer shall be." +
                        "Dive, thoughts, down to my soul: here" +
                        "Clarence comes.",
                "To bait fish withal: if it will feed nothing else," +
                        "it will feed my revenge. He hath disgraced me, and" +
                        "hindered me half a million; laughed at my losses," +
                        "mocked at my gains, scorned my nation, thwarted my" +
                        "bargains, cooled my friends, heated mine" +
                        "enemies; and what's his reason? I am a Jew. Hath" +
                        "not a Jew eyes? hath not a Jew hands, organs," +
                        "dimensions, senses, affections, passions? fed with" +
                        "the same food, hurt with the same weapons, subject" +
                        "to the same diseases, healed by the same means," +
                        "warmed and cooled by the same winter and summer, as" +
                        "a Christian is? If you prick us, do we not bleed?" +
                        "if you tickle us, do we not laugh? if you poison" +
                        "us, do we not die? and if you wrong us, shall we not" +
                        "revenge? If we are like you in the rest, we will" +
                        "resemble you in that. If a Jew wrong a Christian," +
                        "what is his humility? Revenge. If a Christian" +
                        "wrong a Jew, what should his sufferance be by" +
                        "Christian example? Why, revenge. The villany you" +
                        "teach me, I will execute, and it shall go hard but I" +
                        "will better the instruction.",
                "Virtue! a fig! 'tis in ourselves that we are thus" +
                        "or thus. Our bodies are our gardens, to the which" +
                        "our wills are gardeners: so that if we will plant" +
                        "nettles, or sow lettuce, set hyssop and weed up" +
                        "thyme, supply it with one gender of herbs, or" +
                        "distract it with many, either to have it sterile" +
                        "with idleness, or manured with industry, why, the" +
                        "power and corrigible authority of this lies in our" +
                        "wills. If the balance of our lives had not one" +
                        "scale of reason to poise another of sensuality, the" +
                        "blood and baseness of our natures would conduct us" +
                        "to most preposterous conclusions: but we have" +
                        "reason to cool our raging motions, our carnal" +
                        "stings, our unbitted lusts, whereof I take this that" +
                        "you call love to be a sect or scion.",
                "Blow, winds, and crack your cheeks! rage! blow!" +
                        "You cataracts and hurricanoes, spout" +
                        "Till you have drench'd our steeples, drown'd the cocks!" +
                        "You sulphurous and thought-executing fires," +
                        "Vaunt-couriers to oak-cleaving thunderbolts," +
                        "Singe my white head! And thou, all-shaking thunder," +
                        "Smite flat the thick rotundity o' the world!" +
                        "Crack nature's moulds, an germens spill at once," +
                        "That make ingrateful man!"
        )

        /**
         * Our data, part 3.
         */
        private val mExpanded = booleanArrayOf(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        )

    }

    /**
     * We will use a [SpeechView] to display each speech. It's just a [LinearLayout]
     * with two text fields.
     */
    private inner class SpeechView(
            context: Context?,
            title: String?,
            dialogue: String?,
            expanded: Boolean
    ) : LinearLayout(context) {

        /**
         * [TextView] we use to display the title of our dialog
         */
        private val mTitle: TextView

        /**
         * [TextView] we use to display our dialog
         */
        private val mDialogue: TextView

        /**
         * Convenience method to set the title of our [SpeechView]. We just set the text of
         * [TextView] field [mTitle] to our [String] parameter [title]
         *
         * @param title String to use as the text for `TextView mTitle`
         */
        fun setTitle(title: String?) {
            mTitle.text = title
        }

        /**
         * Convenience method to set the dialogue of a [SpeechView]. We just set the text of
         * [TextView] field [mDialogue] to our [String] parameter [words].
         *
         * @param words [String] to use as the text for `TextView mDialogue`
         */
        fun setDialogue(words: String?) {
            mDialogue.text = words
        }

        /**
         * Convenience method to expand or hide the dialogue. We set the visibility of [mDialogue]
         * to VISIBLE if [expanded] is true, or GONE if it is false.
         *
         * @param expanded flag to indicate whether [TextView] field [mDialogue] should be visible
         * (true) or gone (false)
         */
        fun setExpanded(expanded: Boolean) {
            mDialogue.visibility = if (expanded) View.VISIBLE else View.GONE
        }

        /**
         * The init block of our constructor. First we set our orientation to VERTICAL. We create a
         * new instance for `TextView` field `mTitle`, set its text to our `String` parameter `title`
         * and add it to our `LinearLayout` using a new instan of `LayoutParams` specifying a width
         * of MATCH_PARENT, and a height of WRAP_CONTENT. We create a new instance for `TextView`
         * field `mDialogue`, set its text to our `String` parameter `dialogue` and add it to our
         * `LinearLayout` using a new instance of `LayoutParams` specifying a width of MATCH_PARENT,
         * and a height of WRAP_CONTENT. Finally we set the visibility of `mDialogue` to VISIBLE if
         * `expanded` is true, or GONE if it is false.
         */
        init {
            this.orientation = VERTICAL

            // Here we build the child views in code. They could also have
            // been specified in an XML file.
            mTitle = TextView(context)
            mTitle.text = title
            addView(mTitle, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            mDialogue = TextView(context)
            mDialogue.text = dialogue
            addView(mDialogue, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            mDialogue.visibility = if (expanded) View.VISIBLE else View.GONE
        }
    }
}
