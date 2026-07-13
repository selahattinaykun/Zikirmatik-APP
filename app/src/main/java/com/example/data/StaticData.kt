package com.example.data

import com.example.R

data class Surah(
    val name: String, 
    val arabicText: String,
    val turkishMeaning: String,
    val audioResId: Int? = null
)

object StaticData {
    val dailyPrayers = listOf(
        "Rabbenâ âtinâ fi'd-dünyâ haseneten ve fi'l-âhireti haseneten ve kınâ azâbe'n-nâr.",
        "Rabbenâğfirlî ve li-vâlideyye ve lil-mü'minîne yevme yekûmü'l-hisâb.",
        "Allahım, fayda vermeyen ilimden, ürpermeyen kalpten, doymayan nefisten ve kabul olunmayan duadan sana sığınırım.",
        "Ey kalpleri halden hale çeviren Allahım! Kalbimi dinin üzere sabit kıl.",
        "Ezan Duası:\nAllahümme rabbe hâzihî'd-da'veti't-tâmmeh. Ve's-salâti'l-kâimeh. Âti Muhammeden'il-vesîlete ve'l-fadîlete. Veb'ashü mekâmen mahmûdeni'l-lezî veadteh.",
        "Yemek Duası:\nElhamdülillâhi'llezî et'amenâ ve sekânâ ve cealenâ minel-müslimîn. Nîmet-i celîlullah, berekât-ı Halîlullah, şefaat yâ Resûlullah."
    )

    val dailySurahs = listOf(
        Surah("Fatiha Suresi", "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ. الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ. الرَّحْمَنِ الرَّحِيمِ. مَالِكِ يَوْمِ الدِّينِ. إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ. اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ. صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "Bismillâhirrahmânirrahîm. Hamd, âlemlerin Rabbi, Rahmân, Rahîm, hesap ve ceza gününün maliki Allah'a mahsustur. Yalnız sana ibadet ederiz ve yalnız senden yardım dileriz. Bizi doğru yola, kendilerine nimet verdiklerinin yoluna ilet; gazaba uğrayanlarınkine ve sapıklarınkine değil.", R.raw.fatiha),
        Surah("Ayet-el Kürsi", "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ", "Allah kendisinden başka hiçbir ilah olmayandır. Diridir, kayyumdur. Onu ne bir uyuklama tutabilir, ne de bir uyku. Göklerdeki her şey, yerdeki her şey onundur. İzni olmadan onun katında şefaatte bulunacak kimdir? O, kulların önlerindekileri ve arkalarındakileri bilir. Onlar onun ilminden, kendisinin dilediği kadarından başka bir şey kavrayamazlar. Onun kürsüsü gökleri ve yeri kaplamıştır. Onları korumak ona ağır gelmez. O, yücedir, büyüktür.", R.raw.ayet_el_kursi),
        Surah("İhlas Suresi", "قُلْ هُوَ اللَّهُ أَحَدٌ. اللَّهُ الصَّمَدُ. لَمْ يَلِدْ وَلَمْ يُولَدْ. وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ", "De ki: O, Allah'tır, bir tektir. Allah Samed'dir. (Her şey O'na muhtaçtır, o, hiçbir şeye muhtaç değildir.) Ondan çocuk olmamıştır. Kendisi de doğmamıştır. Hiçbir şey O'na denk ve benzer değildir.", R.raw.ihlas),
        Surah("Felak Suresi", "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ. مِنْ شَرِّ مَا خَلَقَ. وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ. وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ. وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ", "De ki: Yarattığı şeylerin kötülüğünden, karanlığı çöktüğü zaman gecenin kötülüğünden, düğümlere üfleyenlerin kötülüğünden, haset ettiği zaman hasetçinin kötülüğünden, sabah aydınlığının Rabbine sığınırım.", R.raw.felak),
        Surah("Nas Suresi", "قُلْ أَعُوذُ بِرَبِّ النَّاسِ. مَلِكِ النَّاسِ. إِلَهِ النَّاسِ. مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ. الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ. مِنَ الْجِنَّةِ وَالنَّاسِ", "De ki: Cinlerden ve insanlardan; insanların kalplerine vesvese veren sinsi vesvesecinin kötülüğünden, insanların Rabbine, insanların Melik'ine, insanların İlah'ına sığınırım.", R.raw.nas),
        Surah("Yasin Suresi", "يس. وَالْقُرْآنِ الْحَكِيمِ. إِنَّكَ لَمِنَ الْمُرْسَلِينَ. عَلَى صِرَاطٍ مُسْتَقِيمٍ... (Sure uzundur, sadece başı verilmiştir)", "Yâ Sîn. Hikmet dolu Kur'an'a andolsun ki, sen şüphesiz peygamberlerdensin. Doğru yol üzerindesin... (Sure uzundur)", R.raw.yasin),
        Surah("Mülk Suresi", "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ... (Sure uzundur)", "Hükümranlık elinde olan Allah yücedir ve O her şeye hakkıyla gücü yetendir... (Sure uzundur)", R.raw.mulk),
        Surah("Tarık Suresi", "وَالسَّمَاءِ وَالطَّارِقِ. وَمَا أَدْرَاكَ مَا الطَّارِقُ. النَّجْمُ الثَّاقِبُ... (Sure uzundur)", "Göğe ve Tarık'a andolsun. Tarık'ın ne olduğunu sen ne bileceksin? O, karanlığı delen yıldızdır... (Sure uzundur)", R.raw.tarik),
        Surah("Beyyine Suresi", "لَمْ يَكُنِ الَّذِينَ كَفَرُوا مِنْ أَهْلِ الْكِتَابِ وَالْمُشْرِكِينَ مُنْفَكِّينَ حَتَّى تَأْتِيَهُمُ الْبَيِّنَةُ... (Sure uzundur)", "Kitap ehlinden ve Allah'a ortak koşanlardan inkar edenler, kendilerine apaçık bir delil gelinceye kadar inkarlarından ayrılacak değillerdi... (Sure uzundur)", R.raw.beyyine),
        Surah("Fetih Suresi", "إِنَّا فَتَحْنَا لَكَ فَتْحًا مُبِينًا... (Sure uzundur)", "Şüphesiz biz sana apaçık bir fetih verdik... (Sure uzundur)", R.raw.fetih)
    )

    val quotes = listOf(
        "Kalpler ancak Allah'ı anmakla huzur bulur. (Rad Suresi 28)",
        "Sabır, imanın yarısıdır. (Hadis-i Şerif)",
        "Dua, müminin silahıdır. (Hadis-i Şerif)",
        "İnsanların en hayırlısı insanlara faydalı olandır. (Hadis-i Şerif)",
        "Namaz dinin direğidir. (Hadis-i Şerif)",
        "Güzel söz sadakadır. (Hadis-i Şerif)",
        "Sizin en hayırlınız Kur'an'ı öğrenen ve öğretendir. (Hadis-i Şerif)",
        "Zorlaştırmayın, kolaylaştırın, nefret ettirmeyin, müjdeleyin. (Hadis-i Şerif)",
        "Ameller niyetlere göredir. (Hadis-i Şerif)",
        "Kim bir iyilik yaparsa ona on katı vardır. (En'am Suresi 160)"
    )

    val zikirler = listOf(
        "Subhanallah",
        "Elhamdülillah",
        "Allahu Ekber",
        "La İlahe İllallah",
        "Estağfirullah",
        "Subhanallahi ve bihamdihi"
    )
}
