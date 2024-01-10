package com.danamon.autochain.constant.financing;

public enum FinancingStatus {
    PENDING, // not yet approved
    ONGOING, // ongoing financing
    OUTSTANDING, // approved financing
    REJECTED, // rejected financing
    COMPLETED // financing done paid

//    Pending: financing request menunggu approval dari BO Danamon
//    Rejected: financing request direject BO
//    Ongoing: financing request diaccept BO
//    Outstanding: terdapat payment atas financing yang belum terbayar (jika sudah terbayar akan terganti ke Ongoing/Completed)
//    Completed: semua payment atas financing telah terbayarkan
}
