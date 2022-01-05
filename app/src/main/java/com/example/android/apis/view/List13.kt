/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.android.apis.R

/**
 * Demonstrates how a list can avoid expensive operations during scrolls or flings. In this
 * case, we pretend that binding a view to its data is slow (even though it really isn't). When
 * a scroll/fling is happening, the adapter binds the view to temporary data. After the scroll/fling
 * has finished, the temporary data is replaced with the actual data.
 */
@SuppressLint("SetTextI18n")
class List13 : AppCompatActivity(), AbsListView.OnScrollListener {
    /**
     * [TextView] in our layout that we use to display the scrolling status in our
     * `onScrollStateChanged` callback: SCROLL_STATE_IDLE "Idle",
     * SCROLL_STATE_TOUCH_SCROLL "Touch scroll", or SCROLL_STATE_FLING "Fling".
     */
    private var mStatus: TextView? = null

    /**
     * Flag used to indicate that scrolling is in progress, set to true or false in our
     * `onScrollStateChanged` callback
     */
    private var mBusy = false

    /**
     * Will not bind views while the list is scrolling
     */
    private inner class SlowAdapter(context: Context) : BaseAdapter() {
        /**
         * [LayoutInflater] instance we use to inflate our item views in our [getView] override.
         */
        private val mInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /**
         * How many items are in the data set represented by this Adapter. The number of items in
         * the list is determined by the number of cheeses in our [String] array field [mStrings].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mStrings.size
        }

        /**
         * Get the data item associated with the specified position in the data set. Since the data
         * comes from an array, just returning the index is sufficient to get at the data. If we were
         * using a more complex data structure, we would return whatever object represents one row in
         * the list.
         *
         * @param position Position of the item within the adapter's data set whose data we want.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return position
        }

        /**
         * Get the row id associated with the specified position in the list. We use the array index
         * as a unique id.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Get a [View] that displays the data at the specified position in the data set. First we
         * declare [TextView] variable `val text`. If [convertView] is null, we use [mInflater]
         * to inflate the layout file android.R.layout.simple_list_item_1, using [parent] to
         * provide layout parameters to initialize `text`. If [convertView] is not null we cast it
         * to [TextView] in order to set `text`.
         *
         * If our flag [mBusy] is false we set the text of `text` to the string at index [position]
         * in [mStrings], and set its tag to null. If [mBusy] is true we set the text of `text` to
         * the string "Loading..." and set the tag to "this".
         *
         * Finally we return `text` to the caller.
         *
         * @param position    The position of the item within the adapter's data set whose view we want.
         * @param convertView The old [View] to reuse, if possible.
         * @param parent      The parent that this [View] will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val text: TextView = if (convertView == null) {
                mInflater.inflate(
                    android.R.layout.simple_list_item_1,
                    parent,
                    false
                ) as TextView
            } else {
                convertView as TextView
            }
            if (!mBusy) {
                text.text = mStrings[position]
                // Null tag means the view has the correct data
                text.tag = null
            } else {
                text.text = "Loading..."
                // Non-null tag means the view still needs to load it's data
                text.tag = this
            }
            return text
        }
    }

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to our layout file R.layout.list_13, and initialize
     * our [ListView] variable `val list` by finding the view with ID [R.id.list]. We initialize
     * our field [mStatus] by finding the view with ID R.id.status and set its text to the string
     * "Idle". We set the list adapter of `list` to a new instance of [SlowAdapter], and we set its
     * `OnScrollListener` to "this".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_13)
        val list: ListView = findViewById(R.id.list)
        mStatus = findViewById(R.id.status)
        mStatus!!.text = "Idle"

        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        list.adapter = SlowAdapter(this)
        list.setOnScrollListener(this)
    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. We ignore it.
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    override fun onScroll(
        view: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
    }

    /**
     * Callback method to be invoked while the list view or grid view is being scrolled. We switch
     * the value of [scrollState]:
     *
     *  * SCROLL_STATE_IDLE - we set our flag [mBusy] to false, initialize `val first`
     *  to the position value of the first visible position, and `val count` to the number
     *  of children in [view]. Then we loop over `i` from 0 to `count-1` setting [TextView]
     *  `val t` to each child in turn, and if the tag of `t` is not null we set the text of
     *  `t` to the string at index `first` plus `i` in [mStrings], and set  its tag to null.
     *  We then set the text of [mStatus] to the string "Idle" and break.
     *
     *  * SCROLL_STATE_TOUCH_SCROLL - we set our flag [mBusy] to true, set the text of
     *  [mStatus] to the string "Touch scroll", and break
     *
     *  * SCROLL_STATE_FLING - we set our flag [mBusy] to true, set the text of
     *  [mStatus] to the string "Fling", and break
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. One of SCROLL_STATE_TOUCH_SCROLL,
     * SCROLL_STATE_IDLE, SCROLL_STATE_FLING.
     */
    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        when (scrollState) {
            AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                mBusy = false
                val first = view.firstVisiblePosition
                val count = view.childCount
                var i = 0
                while (i < count) {
                    val t = view.getChildAt(i) as TextView
                    if (t.tag != null) {
                        t.text = mStrings[first + i]
                        t.tag = null
                    }
                    i++
                }
                mStatus!!.text = "Idle"
            }
            AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL -> {
                mBusy = true
                mStatus!!.text = "Touch scroll"
            }
            AbsListView.OnScrollListener.SCROLL_STATE_FLING -> {
                mBusy = true
                mStatus!!.text = "Fling"
            }
        }
    }

    /**
     * Our list of cheeses that we use as our database.
     */
    private val mStrings = arrayOf(
        "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam",
        "Abondance", "Ackawi", "Acorn", "Adelost", "Affidelice au Chablis",
        "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
        "Allgauer Emmentaler", "Alverca", "Ambert", "American Cheese",
        "Ami du Chambertin", "Anejo Enchilado", "Anneau du Vic-Bilh",
        "Anthoriro", "Appenzell", "Aragon", "Ardi Gasna", "Ardrahan",
        "Armenian String", "Aromes au Gene de Marc", "Asadero", "Asiago",
        "Aubisque Pyrenees", "Autun", "Avaxtskyr", "Baby Swiss", "Babybel",
        "Baguette Laonnaise", "Bakers", "Baladi", "Balaton", "Bandal",
        "Banon", "Barry's Bay Cheddar", "Basing", "Basket Cheese",
        "Bath Cheese", "Bavarian Bergkase", "Baylough", "Beaufort",
        "Beauvoorde", "Beenleigh Blue", "Beer Cheese", "Bel Paese",
        "Bergader", "Bergere Bleue", "Berkswell", "Beyaz Peynir",
        "Bierkase", "Bishop Kennedy", "Blarney", "Bleu d'Auvergne",
        "Bleu de Gex", "Bleu de Laqueuille", "Bleu de Septmoncel",
        "Bleu Des Causses", "Blue", "Blue Castello", "Blue Rathgore",
        "Blue Vein (Australian)", "Blue Vein Cheeses", "Bocconcini",
        "Bocconcini (Australian)", "Boeren Leidenkaas", "Bonchester",
        "Bosworth", "Bougon", "Boule Du Roves", "Boulette d'Avesnes",
        "Boursault", "Boursin", "Bouyssou", "Bra", "Braudostur",
        "Breakfast Cheese", "Brebis du Lavort", "Brebis du Lochois",
        "Brebis du Puyfaucon", "Bresse Bleu", "Brick", "Brie",
        "Brie de Meaux", "Brie de Melun", "Brillat-Savarin", "Brin",
        "Brin d' Amour", "Brin d'Amour", "Brinza (Burduf Brinza)",
        "Briquette de Brebis", "Briquette du Forez", "Broccio",
        "Broccio Demi-Affine", "Brousse du Rove", "Bruder Basil",
        "Brusselae Kaas (Fromage de Bruxelles)", "Bryndza",
        "Buchette d'Anjou", "Buffalo", "Burgos", "Butte", "Butterkase",
        "Button (Innes)", "Buxton Blue", "Cabecou", "Caboc", "Cabrales",
        "Cachaille", "Caciocavallo", "Caciotta", "Caerphilly",
        "Cairnsmore", "Calenzana", "Cambazola", "Camembert de Normandie",
        "Canadian Cheddar", "Canestrato", "Cantal", "Caprice des Dieux",
        "Capricorn Goat", "Capriole Banon", "Carre de l'Est",
        "Casciotta di Urbino", "Cashel Blue", "Castellano", "Castelleno",
        "Castelmagno", "Castelo Branco", "Castigliano", "Cathelain",
        "Celtic Promise", "Cendre d'Olivet", "Cerney", "Chabichou",
        "Chabichou du Poitou", "Chabis de Gatine", "Chaource", "Charolais",
        "Chaumes", "Cheddar", "Cheddar Clothbound", "Cheshire", "Chevres",
        "Chevrotin des Aravis", "Chontaleno", "Civray",
        "Coeur de Camembert au Calvados", "Coeur de Chevre", "Colby",
        "Cold Pack", "Comte", "Coolea", "Cooleney", "Coquetdale",
        "Corleggy", "Cornish Pepper", "Cotherstone", "Cotija",
        "Cottage Cheese", "Cottage Cheese (Australian)", "Cougar Gold",
        "Coulommiers", "Coverdale", "Crayeux de Roncq", "Cream Cheese",
        "Cream Havarti", "Crema Agria", "Crema Mexicana", "Creme Fraiche",
        "Crescenza", "Croghan", "Crottin de Chavignol",
        "Crottin du Chavignol", "Crowdie", "Crowley", "Cuajada", "Curd",
        "Cure Nantais", "Curworthy", "Cwmtawe Pecorino",
        "Cypress Grove Chevre", "Danablu (Danish Blue)", "Danbo",
        "Danish Fontina", "Daralagjazsky", "Dauphin", "Delice des Fiouves",
        "Denhany Dorset Drum", "Derby", "Dessertnyj Belyj", "Devon Blue",
        "Devon Garland", "Dolcelatte", "Doolin", "Doppelrhamstufel",
        "Dorset Blue Vinney", "Double Gloucester", "Double Worcester",
        "Dreux a la Feuille", "Dry Jack", "Duddleswell", "Dunbarra",
        "Dunlop", "Dunsyre Blue", "Duroblando", "Durrus",
        "Dutch Mimolette (Commissiekaas)", "Edam", "Edelpilz",
        "Emental Grand Cru", "Emlett", "Emmental", "Epoisses de Bourgogne",
        "Esbareich", "Esrom", "Etorki", "Evansdale Farmhouse Brie",
        "Evora De L'Alentejo", "Exmoor Blue", "Explorateur", "Feta",
        "Feta (Australian)", "Figue", "Filetta", "Fin-de-Siecle",
        "Finlandia Swiss", "Finn", "Fiore Sardo", "Fleur du Maquis",
        "Flor de Guia", "Flower Marie", "Folded",
        "Folded cheese with mint", "Fondant de Brebis", "Fontainebleau",
        "Fontal", "Fontina Val d'Aosta", "Formaggio di capra", "Fougerus",
        "Four Herb Gouda", "Fourme d' Ambert", "Fourme de Haute Loire",
        "Fourme de Montbrison", "Fresh Jack", "Fresh Mozzarella",
        "Fresh Ricotta", "Fresh Truffles", "Fribourgeois", "Friesekaas",
        "Friesian", "Friesla", "Frinault", "Fromage a Raclette",
        "Fromage Corse", "Fromage de Montagne de Savoie", "Fromage Frais",
        "Fruit Cream Cheese", "Frying Cheese", "Fynbo", "Gabriel",
        "Galette du Paludier", "Galette Lyonnaise",
        "Galloway Goat's Milk Gems", "Gammelost", "Gaperon a l'Ail",
        "Garrotxa", "Gastanberra", "Geitost", "Gippsland Blue", "Gjetost",
        "Gloucester", "Golden Cross", "Gorgonzola", "Gornyaltajski",
        "Gospel Green", "Gouda", "Goutu", "Gowrie", "Grabetto", "Graddost",
        "Grafton Village Cheddar", "Grana", "Grana Padano", "Grand Vatel",
        "Grataron d' Areches", "Gratte-Paille", "Graviera", "Greuilh",
        "Greve", "Gris de Lille", "Gruyere", "Gubbeen", "Guerbigny",
        "Halloumi", "Halloumy (Australian)", "Haloumi-Style Cheese",
        "Harbourne Blue", "Havarti", "Heidi Gruyere", "Hereford Hop",
        "Herrgardsost", "Herriot Farmhouse", "Herve", "Hipi Iti",
        "Hubbardston Blue Cow", "Hushallsost", "Iberico", "Idaho Goatster",
        "Idiazabal", "Il Boschetto al Tartufo", "Ile d'Yeu",
        "Isle of Mull", "Jarlsberg", "Jermi Tortes", "Jibneh Arabieh",
        "Jindi Brie", "Jubilee Blue", "Juustoleipa", "Kadchgall", "Kaseri",
        "Kashta", "Kefalotyri", "Kenafa", "Kernhem", "Kervella Affine",
        "Kikorangi", "King Island Cape Wickham Brie", "King River Gold",
        "Klosterkaese", "Knockalara", "Kugelkase", "L'Aveyronnais",
        "L'Ecir de l'Aubrac", "La Taupiniere", "La Vache Qui Rit",
        "Laguiole", "Lairobell", "Lajta", "Lanark Blue", "Lancashire",
        "Langres", "Lappi", "Laruns", "Lavistown", "Le Brin",
        "Le Fium Orbo", "Le Lacandou", "Le Roule", "Leafield", "Lebbene",
        "Leerdammer", "Leicester", "Leyden", "Limburger",
        "Lincolnshire Poacher", "Lingot Saint Bousquet d'Orb", "Liptauer",
        "Little Rydings", "Livarot", "Llanboidy", "Llanglofan Farmhouse",
        "Loch Arthur Farmhouse", "Loddiswell Avondale", "Longhorn",
        "Lou Palou", "Lou Pevre", "Lyonnais", "Maasdam", "Macconais",
        "Mahoe Aged Gouda", "Mahon", "Malvern", "Mamirolle", "Manchego",
        "Manouri", "Manur", "Marble Cheddar", "Marbled Cheeses",
        "Maredsous", "Margotin", "Maribo", "Maroilles", "Mascares",
        "Mascarpone", "Mascarpone (Australian)", "Mascarpone Torta",
        "Matocq", "Maytag Blue", "Meira", "Menallack Farmhouse",
        "Menonita", "Meredith Blue", "Mesost", "Metton (Cancoillotte)",
        "Meyer Vintage Gouda", "Mihalic Peynir", "Milleens", "Mimolette",
        "Mine-Gabhar", "Mini Baby Bells", "Mixte", "Molbo",
        "Monastery Cheeses", "Mondseer", "Mont D'or Lyonnais", "Montasio",
        "Monterey Jack", "Monterey Jack Dry", "Morbier",
        "Morbier Cru de Montagne", "Mothais a la Feuille", "Mozzarella",
        "Mozzarella (Australian)", "Mozzarella di Bufala",
        "Mozzarella Fresh, in water", "Mozzarella Rolls", "Munster",
        "Murol", "Mycella", "Myzithra", "Naboulsi", "Nantais",
        "Neufchatel", "Neufchatel (Australian)", "Niolo", "Nokkelost",
        "Northumberland", "Oaxaca", "Olde York", "Olivet au Foin",
        "Olivet Bleu", "Olivet Cendre", "Orkney Extra Mature Cheddar",
        "Orla", "Oschtjepka", "Ossau Fermier", "Ossau-Iraty", "Oszczypek",
        "Oxford Blue", "P'tit Berrichon", "Palet de Babligny", "Paneer",
        "Panela", "Pannerone", "Pant ys Gawn", "Parmesan (Parmigiano)",
        "Parmigiano Reggiano", "Pas de l'Escalette", "Passendale",
        "Pasteurized Processed", "Pate de Fromage", "Patefine Fort",
        "Pave d'Affinois", "Pave d'Auge", "Pave de Chirac",
        "Pave du Berry", "Pecorino", "Pecorino in Walnut Leaves",
        "Pecorino Romano", "Peekskill Pyramid", "Pelardon des Cevennes",
        "Pelardon des Corbieres", "Penamellera", "Penbryn", "Pencarreg",
        "Perail de Brebis", "Petit Morin", "Petit Pardou", "Petit-Suisse",
        "Picodon de Chevre", "Picos de Europa", "Piora",
        "Pithtviers au Foin", "Plateau de Herve", "Plymouth Cheese",
        "Podhalanski", "Poivre d'Ane", "Polkolbin", "Pont l'Eveque",
        "Port Nicholson", "Port-Salut", "Postel", "Pouligny-Saint-Pierre",
        "Pourly", "Prastost", "Pressato", "Prince-Jean",
        "Processed Cheddar", "Provolone", "Provolone (Australian)",
        "Pyengana Cheddar", "Pyramide", "Quark", "Quark (Australian)",
        "Quartirolo Lombardo", "Quatre-Vents", "Quercy Petit",
        "Queso Blanco", "Queso Blanco con Frutas --Pina y Mango",
        "Queso de Murcia", "Queso del Montsec", "Queso del Tietar",
        "Queso Fresco", "Queso Fresco (Adobera)", "Queso Iberico",
        "Queso Jalapeno", "Queso Majorero", "Queso Media Luna",
        "Queso Para Frier", "Queso Quesadilla", "Rabacal", "Raclette",
        "Ragusano", "Raschera", "Reblochon", "Red Leicester",
        "Regal de la Dombes", "Reggianito", "Remedou", "Requeson",
        "Richelieu", "Ricotta", "Ricotta (Australian)", "Ricotta Salata",
        "Ridder", "Rigotte", "Rocamadour", "Rollot", "Romano",
        "Romans Part Dieu", "Roncal", "Roquefort", "Roule",
        "Rouleau De Beaulieu", "Royalp Tilsit", "Rubens", "Rustinu",
        "Saaland Pfarr", "Saanenkaese", "Saga", "Sage Derby",
        "Sainte Maure", "Saint-Marcellin", "Saint-Nectaire",
        "Saint-Paulin", "Salers", "Samso", "San Simon", "Sancerre",
        "Sap Sago", "Sardo", "Sardo Egyptian", "Sbrinz", "Scamorza",
        "Schabzieger", "Schloss", "Selles sur Cher", "Selva", "Serat",
        "Seriously Strong Cheddar", "Serra da Estrela", "Sharpam",
        "Shelburne Cheddar", "Shropshire Blue", "Siraz", "Sirene",
        "Smoked Gouda", "Somerset Brie", "Sonoma Jack",
        "Sottocenare al Tartufo", "Soumaintrain", "Sourire Lozerien",
        "Spenwood", "Sraffordshire Organic", "St. Agur Blue Cheese",
        "Stilton", "Stinking Bishop", "String", "Sussex Slipcote",
        "Sveciaost", "Swaledale", "Sweet Style Swiss", "Swiss",
        "Syrian (Armenian String)", "Tala", "Taleggio", "Tamie",
        "Tasmania Highland Chevre Log", "Taupiniere", "Teifi", "Telemea",
        "Testouri", "Tete de Moine", "Tetilla", "Texas Goat Cheese",
        "Tibet", "Tillamook Cheddar", "Tilsit", "Timboon Brie", "Toma",
        "Tomme Brulee", "Tomme d'Abondance", "Tomme de Chevre",
        "Tomme de Romans", "Tomme de Savoie", "Tomme des Chouans",
        "Tommes", "Torta del Casar", "Toscanello", "Touree de L'Aubier",
        "Tourmalet", "Trappe (Veritable)", "Trois Cornes De Vendee",
        "Tronchon", "Trou du Cru", "Truffe", "Tupi", "Turunmaa",
        "Tymsboro", "Tyn Grug", "Tyning", "Ubriaco", "Ulloa",
        "Vacherin-Fribourgeois", "Valencay", "Vasterbottenost", "Venaco",
        "Vendomois", "Vieux Corse", "Vignotte", "Vulscombe",
        "Waimata Farmhouse Blue", "Washed Rind Cheese (Australian)",
        "Waterloo", "Weichkaese", "Wellington", "Wensleydale",
        "White Stilton", "Whitestone Farmhouse", "Wigmore",
        "Woodside Cabecou", "Xanadu", "Xynotyro", "Yarg Cornish",
        "Yarra Valley Pyramid", "Yorkshire Blue", "Zamorano",
        "Zanetti Grana Padano", "Zanetti Parmigiano Reggiano")
}