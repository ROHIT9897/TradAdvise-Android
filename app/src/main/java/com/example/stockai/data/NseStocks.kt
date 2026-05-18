package com.example.stockai.data

// ── NSE Master Stocks File ────────────────────────────────
// Used by: Search, Watchlist, Horizon, Market tab
// Single source of truth for all stock data

data class NseStock(
    val ticker:  String,
    val name:    String,
    val sector:  String
)

object NseStocks {

    val ALL: List<NseStock> = listOf(

        // ── NIFTY 50 ──────────────────────────────────────
        NseStock("RELIANCE",   "Reliance Industries",         "Energy"),
        NseStock("TCS",        "Tata Consultancy Services",   "IT"),
        NseStock("HDFCBANK",   "HDFC Bank",                   "Banking"),
        NseStock("INFY",       "Infosys",                     "IT"),
        NseStock("ICICIBANK",  "ICICI Bank",                  "Banking"),
        NseStock("HINDUNILVR", "Hindustan Unilever",          "FMCG"),
        NseStock("SBIN",       "State Bank of India",         "Banking"),
        NseStock("BHARTIARTL", "Bharti Airtel",               "Telecom"),
        NseStock("ITC",        "ITC Limited",                 "FMCG"),
        NseStock("KOTAKBANK",  "Kotak Mahindra Bank",         "Banking"),
        NseStock("LT",         "Larsen and Toubro",           "Infrastructure"),
        NseStock("AXISBANK",   "Axis Bank",                   "Banking"),
        NseStock("ASIANPAINT", "Asian Paints",                "Paints"),
        NseStock("MARUTI",     "Maruti Suzuki",               "Auto"),
        NseStock("SUNPHARMA",  "Sun Pharmaceutical",          "Pharma"),
        NseStock("TITAN",      "Titan Company",               "Jewellery"),
        NseStock("BAJFINANCE", "Bajaj Finance",               "NBFC"),
        NseStock("WIPRO",      "Wipro",                       "IT"),
        NseStock("ULTRACEMCO", "UltraTech Cement",            "Cement"),
        NseStock("NESTLEIND",  "Nestle India",                "FMCG"),
        NseStock("POWERGRID",  "Power Grid Corporation",      "Power"),
        NseStock("NTPC",       "NTPC Limited",                "Power"),
        NseStock("BAJAJFINSV", "Bajaj Finserv",               "NBFC"),
        NseStock("HCLTECH",    "HCL Technologies",            "IT"),
        NseStock("ONGC",       "Oil and Natural Gas",         "Energy"),
        NseStock("COALINDIA",  "Coal India",                  "Mining"),
        NseStock("TATAMOTORS", "Tata Motors",                 "Auto"),
        NseStock("ADANIENT",   "Adani Enterprises",           "Conglomerate"),
        NseStock("ADANIPORTS", "Adani Ports",                 "Ports"),
        NseStock("JSWSTEEL",   "JSW Steel",                   "Steel"),
        NseStock("TATASTEEL",  "Tata Steel",                  "Steel"),
        NseStock("HINDALCO",   "Hindalco Industries",         "Metals"),
        NseStock("TECHM",      "Tech Mahindra",               "IT"),
        NseStock("EICHERMOT",  "Eicher Motors",               "Auto"),
        NseStock("HEROMOTOCO", "Hero MotoCorp",               "Auto"),
        NseStock("DRREDDY",    "Dr Reddys Laboratories",      "Pharma"),
        NseStock("CIPLA",      "Cipla",                       "Pharma"),
        NseStock("APOLLOHOSP", "Apollo Hospitals",            "Healthcare"),
        NseStock("DIVISLAB",   "Divis Laboratories",          "Pharma"),
        NseStock("BPCL",       "Bharat Petroleum",            "Energy"),
        NseStock("GRASIM",     "Grasim Industries",           "Cement"),
        NseStock("BRITANNIA",  "Britannia Industries",        "FMCG"),
        NseStock("TATACONSUM", "Tata Consumer Products",      "FMCG"),
        NseStock("INDUSINDBK", "IndusInd Bank",               "Banking"),
        NseStock("SBILIFE",    "SBI Life Insurance",          "Insurance"),
        NseStock("HDFCLIFE",   "HDFC Life Insurance",         "Insurance"),
        NseStock("M&M",        "Mahindra and Mahindra",       "Auto"),
        NseStock("ICICIPRULI", "ICICI Prudential Life",       "Insurance"),
        NseStock("SHREECEM",   "Shree Cement",                "Cement"),
        NseStock("BAJAJ-AUTO", "Bajaj Auto",                  "Auto"),

        // ── IT ────────────────────────────────────────────
        NseStock("PERSISTENT", "Persistent Systems",          "IT"),
        NseStock("MPHASIS",    "Mphasis",                     "IT"),
        NseStock("LTIM",       "LTIMindtree",                 "IT"),
        NseStock("COFORGE",    "Coforge",                     "IT"),
        NseStock("OFSS",       "Oracle Financial Services",   "IT"),
        NseStock("KPIT",       "KPIT Technologies",           "IT"),
        NseStock("TATAELXSI",  "Tata Elxsi",                  "IT"),
        NseStock("FSL",        "Firstsource Solutions",       "IT"),

        // ── BANKING ───────────────────────────────────────
        NseStock("IDFCFIRSTB", "IDFC First Bank",             "Banking"),
        NseStock("FEDERALBNK", "Federal Bank",                "Banking"),
        NseStock("BANDHANBNK", "Bandhan Bank",                "Banking"),
        NseStock("PNB",        "Punjab National Bank",        "Banking"),
        NseStock("BANKBARODA", "Bank of Baroda",              "Banking"),
        NseStock("CANBK",      "Canara Bank",                 "Banking"),
        NseStock("UNIONBANK",  "Union Bank of India",         "Banking"),
        NseStock("INDIANB",    "Indian Bank",                 "Banking"),
        NseStock("RBLBANK",    "RBL Bank",                    "Banking"),

        // ── NBFC & FINANCE ────────────────────────────────
        NseStock("CHOLAFIN",   "Cholamandalam Finance",       "NBFC"),
        NseStock("MUTHOOTFIN", "Muthoot Finance",             "NBFC"),
        NseStock("MANAPPURAM", "Manappuram Finance",          "NBFC"),
        NseStock("SUNDARMFIN", "Sundaram Finance",            "NBFC"),
        NseStock("RECLTD",     "REC Limited",                 "Finance"),
        NseStock("PFC",        "Power Finance Corporation",   "Finance"),
        NseStock("IRFC",       "Indian Railway Finance",      "Finance"),
        NseStock("HUDCO",      "HUDCO",                       "Finance"),

        // ── PHARMA ────────────────────────────────────────
        NseStock("TORNTPHARM", "Torrent Pharmaceuticals",     "Pharma"),
        NseStock("LUPIN",      "Lupin",                       "Pharma"),
        NseStock("AUROPHARMA", "Aurobindo Pharma",            "Pharma"),
        NseStock("ZYDUSLIFE",  "Zydus Lifesciences",          "Pharma"),
        NseStock("IPCALAB",    "IPCA Laboratories",           "Pharma"),
        NseStock("ALKEM",      "Alkem Laboratories",          "Pharma"),
        NseStock("AJANTPHARM", "Ajanta Pharma",               "Pharma"),
        NseStock("GRANULES",   "Granules India",              "Pharma"),

        // ── HEALTHCARE ────────────────────────────────────
        NseStock("LALPATHLAB", "Dr Lal PathLabs",             "Healthcare"),
        NseStock("METROPOLIS", "Metropolis Healthcare",       "Healthcare"),
        NseStock("MAXHEALTH",  "Max Healthcare",              "Healthcare"),
        NseStock("FORTIS",     "Fortis Healthcare",           "Healthcare"),
        NseStock("ASTER",      "Aster DM Healthcare",         "Healthcare"),

        // ── AUTO ──────────────────────────────────────────
        NseStock("MOTHERSON",  "Samvardhana Motherson",       "Auto"),
        NseStock("BOSCHLTD",   "Bosch India",                 "Auto"),
        NseStock("EXIDEIND",   "Exide Industries",            "Auto"),
        NseStock("BALKRISIND", "Balkrishna Industries",       "Auto"),
        NseStock("APOLLOTYRE", "Apollo Tyres",                "Auto"),
        NseStock("MRF",        "MRF Limited",                 "Auto"),

        // ── POWER & ENERGY ────────────────────────────────
        NseStock("TATAPOWER",  "Tata Power",                  "Power"),
        NseStock("ADANIGREEN", "Adani Green Energy",          "Renewable"),
        NseStock("TORNTPOWER", "Torrent Power",               "Power"),
        NseStock("NHPC",       "NHPC Limited",                "Power"),
        NseStock("SJVN",       "SJVN Limited",                "Power"),
        NseStock("IGL",        "Indraprastha Gas",            "Energy"),
        NseStock("MGL",        "Mahanagar Gas",               "Energy"),
        NseStock("PETRONET",   "Petronet LNG",                "Energy"),
        NseStock("IOC",        "Indian Oil Corporation",      "Energy"),
        NseStock("HINDPETRO",  "Hindustan Petroleum",         "Energy"),
        NseStock("ATGL",       "Adani Total Gas",             "Energy"),

        // ── INFRASTRUCTURE ────────────────────────────────
        NseStock("HAL",        "Hindustan Aeronautics",       "Defence"),
        NseStock("BEL",        "Bharat Electronics",          "Defence"),
        NseStock("BDL",        "Bharat Dynamics",             "Defence"),
        NseStock("MAZAGON",    "Mazagon Dock",                "Defence"),
        NseStock("GRSE",       "Garden Reach Shipbuilders",   "Defence"),
        NseStock("RVNL",       "Rail Vikas Nigam",            "Infrastructure"),
        NseStock("IRCON",      "Ircon International",         "Infrastructure"),
        NseStock("NBCC",       "NBCC India",                  "Infrastructure"),
        NseStock("CONCOR",     "Container Corporation",       "Logistics"),

        // ── METALS & MINING ───────────────────────────────
        NseStock("VEDL",       "Vedanta Limited",             "Metals"),
        NseStock("SAIL",       "Steel Authority of India",    "Steel"),
        NseStock("JINDALSTEL", "Jindal Steel and Power",      "Steel"),
        NseStock("NMDC",       "NMDC Limited",                "Mining"),
        NseStock("NATIONALUM", "National Aluminium",          "Metals"),
        NseStock("HINDCOPPER", "Hindustan Copper",            "Metals"),

        // ── FMCG ──────────────────────────────────────────
        NseStock("DABUR",      "Dabur India",                 "FMCG"),
        NseStock("MARICO",     "Marico",                      "FMCG"),
        NseStock("COLPAL",     "Colgate-Palmolive",           "FMCG"),
        NseStock("GODREJCP",   "Godrej Consumer Products",    "FMCG"),
        NseStock("VBL",        "Varun Beverages",             "FMCG"),
        NseStock("BIKAJI",     "Bikaji Foods",                "FMCG"),
        NseStock("RADICO",     "Radico Khaitan",              "FMCG"),

        // ── ELECTRONICS & ELECTRICALS ─────────────────────
        NseStock("POLYCAB",    "Polycab India",               "Electricals"),
        NseStock("HAVELLS",    "Havells India",               "Electricals"),
        NseStock("DIXON",      "Dixon Technologies",          "Electronics"),
        NseStock("VOLTAS",     "Voltas",                      "Electricals"),
        NseStock("AMBER",      "Amber Enterprises",           "Electronics"),

        // ── ENGINEERING ───────────────────────────────────
        NseStock("ABB",        "ABB India",                   "Engineering"),
        NseStock("SIEMENS",    "Siemens India",               "Engineering"),
        NseStock("THERMAX",    "Thermax",                     "Engineering"),
        NseStock("BHEL",       "Bharat Heavy Electricals",    "Engineering"),
        NseStock("CUMMINSIND", "Cummins India",               "Engineering"),

        // ── PAINTS ────────────────────────────────────────
        NseStock("BERGEPAINT", "Berger Paints",               "Paints"),
        NseStock("PIDILITIND", "Pidilite Industries",         "Chemicals"),

        // ── CHEMICALS ─────────────────────────────────────
        NseStock("ATUL",       "Atul Limited",                "Chemicals"),
        NseStock("DEEPAKNTR",  "Deepak Nitrite",              "Chemicals"),
        NseStock("NAVINFLUOR", "Navin Fluorine",              "Chemicals"),
        NseStock("CLEAN",      "Clean Science",               "Chemicals"),
        NseStock("FINEORG",    "Fine Organic",                "Chemicals"),
        NseStock("ALKYLAMINE", "Alkyl Amines",                "Chemicals"),

        // ── INTERNET & TECH ───────────────────────────────
        NseStock("ZOMATO",     "Zomato",                      "Food Tech"),
        NseStock("NAUKRI",     "Info Edge India",             "Internet"),
        NseStock("POLICYBZR",  "PB Fintech",                  "Fintech"),
        NseStock("PAYTM",      "One97 Communications",        "Fintech"),
        NseStock("IRCTC",      "Indian Railway Catering",     "Travel"),
        NseStock("INDIAMART",  "Indiamart Intermesh",         "Internet"),
        NseStock("MAPMYINDIA", "MapmyIndia",                  "Internet"),
        NseStock("EASEMYTRIP", "Easy Trip Planners",          "Travel"),

        // ── RETAIL ────────────────────────────────────────
        NseStock("DMART",      "Avenue Supermarts",           "Retail"),
        NseStock("TRENT",      "Trent Limited",               "Retail"),
        NseStock("PAGEIND",    "Page Industries",             "Textile"),
        NseStock("ABFRL",      "Aditya Birla Fashion",        "Retail"),

        // ── QSR ───────────────────────────────────────────
        NseStock("JUBLFOOD",   "Jubilant Foodworks",          "QSR"),
        NseStock("DEVYANI",    "Devyani International",       "QSR"),
        NseStock("SAPPHIRE",   "Sapphire Foods",              "QSR"),
        NseStock("WESTLIFE",   "Westlife Foodworld",          "QSR"),

        // ── CEMENT ────────────────────────────────────────
        NseStock("AMBUJACEM",  "Ambuja Cements",              "Cement"),
        NseStock("JKCEMENT",   "JK Cement",                   "Cement"),
        NseStock("RAMCOCEM",   "Ramco Cements",               "Cement"),

        // ── LOGISTICS ─────────────────────────────────────
        NseStock("DELHIVERY",  "Delhivery",                   "Logistics"),
        NseStock("BLUEDART",   "Blue Dart Express",           "Logistics"),
    )

    // ── Ticker to stock map ───────────────────────────────
    val tickerMap: Map<String, NseStock> by lazy {
        ALL.associateBy { it.ticker }
    }

    // ── Get all tickers ───────────────────────────────────
    fun getAllTickers(): List<String> = ALL.map { it.ticker }

    // ── Get company name ──────────────────────────────────
    fun getName(ticker: String): String =
        tickerMap[ticker]?.name ?: ticker

    // ── Get sector ────────────────────────────────────────
    fun getSector(ticker: String): String =
        tickerMap[ticker]?.sector ?: "Unknown"

    // ── Search stocks ─────────────────────────────────────
    fun search(query: String, limit: Int = 10): List<NseStock> {
        val q = query.uppercase().trim()
        if (q.isEmpty()) return ALL.take(limit)

        val results = ALL.filter { stock ->
            stock.ticker.contains(q) ||
                    stock.name.uppercase().contains(q) ||
                    stock.sector.uppercase().contains(q)
        }

        // Sort: exact ticker match first, then prefix, then contains
        return results.sortedWith(
            compareBy {
                when {
                    it.ticker == q              -> 0
                    it.ticker.startsWith(q)     -> 1
                    it.name.uppercase().startsWith(q) -> 2
                    else                        -> 3
                }
            }
        ).take(limit)
    }

    // ── Stocks for market movers (top liquid) ─────────────
    val MARKET_MOVER_STOCKS = listOf(
        "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK",
        "SBIN", "HINDUNILVR", "BHARTIARTL", "ITC", "KOTAKBANK",
        "LT", "AXISBANK", "ASIANPAINT", "MARUTI", "SUNPHARMA",
        "TITAN", "BAJFINANCE", "WIPRO", "ULTRACEMCO", "NESTLEIND",
        "POWERGRID", "NTPC", "BAJAJFINSV", "HCLTECH", "ONGC",
        "COALINDIA", "TATAMOTORS", "ADANIENT", "ADANIPORTS", "JSWSTEEL",
        "TATASTEEL", "HINDALCO", "TECHM", "EICHERMOT", "HEROMOTOCO",
        "DRREDDY", "CIPLA", "APOLLOHOSP", "DIVISLAB", "BPCL",
        "GRASIM", "BRITANNIA", "TATACONSUM", "INDUSINDBK", "SBILIFE",
        "HDFCLIFE", "M&M", "ZOMATO", "IRCTC", "HAL",
        "BEL", "DMART", "HAVELLS", "PIDILITIND", "SIEMENS",
        "MUTHOOTFIN", "DABUR", "MARICO", "COLPAL", "TORNTPHARM",
        "LUPIN", "IDFCFIRSTB", "FEDERALBNK", "PNB", "BANKBARODA",
        "VEDL", "RECLTD", "PFC", "IRFC", "SAIL",
        "TATAPOWER", "POLYCAB", "DIXON", "VOLTAS", "VBL",
        "ZYDUSLIFE", "LALPATHLAB", "MAXHEALTH", "FORTIS", "JUBLFOOD",
        "NAUKRI", "TRENT", "PAGEIND", "ABB", "BHEL",
        "BDL", "MAZAGON", "IGL", "PETRONET", "IOC",
    )
}