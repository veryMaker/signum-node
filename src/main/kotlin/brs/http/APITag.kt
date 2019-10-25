package brs.http

enum class APITag constructor(val displayName: String) {
    ACCOUNTS("Accounts"),
    ALIASES("Aliases"),
    AE("Asset Exchange"),
    CREATE_TRANSACTION("Create Transaction"),
    BLOCKS("Blocks"),
    DGS("Digital Goods Store"),
    INFO("Server Info"),
    MESSAGES("Messages"),
    MINING("Mining"),
    TRANSACTIONS("Transactions"),
    AT("Automated Transaction"),
    FEES("Fees"),
    UTILS("Utils"),
    DEBUG("Debug"),
    PEER_INFO("Server Peer Info")
}
