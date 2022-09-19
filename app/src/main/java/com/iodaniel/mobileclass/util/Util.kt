package com.iodaniel.mobileclass.util

import android.net.Uri

object Util {
    fun getFileName(uri: Uri): String {
        var result = ""
        result = uri.path.toString()
        val cut: Int = result.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
        return result
    }

    fun convertMillieToHMmSs(millie: Long): String {
        val seconds = millie / 1000
        val second = seconds % 60
        val minute = seconds / 60 % 60
        val hour = seconds / (60 * 60) % 24
        val result = ""
        return if (hour > 0) {
            String.format("%02d:%02d:%02d", hour, minute, second)
        } else {
            String.format("%02d:%02d", minute, second)
        }
    }

    fun cleanContentPreferences(contentInput: String): String {
        if (contentInput == "") return contentInput
        var content = contentInput
        for (stopword in Util.STOPWORDS) content = content.replace(stopword, " ")
        for (symbol in Util.SYMBOLS) content = content.replace(symbol, ",")
        for (number in NUMBERS) content = content.replace(number, NUMBERS_TEXT[NUMBERS.indexOf(number)])
        content = content.replace(" ", ",")
        content = content.replace(",,", ",")
        content = content.replace(",,,", ",")
        content = content.replace(",,,,", ",")
        return content
    }

    fun removeComma(input: String) = input.split(",")

    val NUMBERS = arrayListOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    val NUMBERS_TEXT = arrayListOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Zero")

    val SYMBOLS = arrayListOf(
        ".", ",", "/", "?", "'", ";", ":", "|", "\n", "]", "[", "{", "}", "=", "+", "-", "_", ")", "(", "*", "&", "^", "%", "$", "#", "@", "!", "`", "~", " "
    )

    val STOPWORDS = arrayListOf(
        " i ",
        " me ",
        " my ",
        " myself ",
        " we ",
        " our ",
        " ours ",
        " ourselves ",
        " you ",
        " you're ",
        " you've ",
        " you'll ",
        " you'd ",
        " your ",
        " yours ",
        " yourself ",
        " yourselves ",
        " he ",
        " him ",
        " his ",
        " himself ",
        " she ",
        " she's ",
        " her ",
        " hers ",
        " herself ",
        " it ",
        " it's ",
        " its ",
        " itself ",
        " they ",
        " them ",
        " their ",
        " theirs ",
        " themselves ",
        " what ",
        " which ",
        " who ",
        " whom ",
        " this ",
        " that ",
        " that'll ",
        " these ",
        " those ",
        " am ",
        " is ",
        " are ",
        " was ",
        " were ",
        " be ",
        " been ",
        " being ",
        " have ",
        " has ",
        " had ",
        " having ",
        " do ",
        " does ",
        " did ",
        " doing ",
        " a ",
        " an ",
        " the ",
        " and ",
        " but ",
        " if ",
        " or ",
        " because ",
        " as ",
        " until ",
        " while ",
        " of ",
        " at ",
        " by ",
        " for ",
        " with ",
        " about ",
        " against ",
        " between ",
        " into ",
        " through ",
        " during ",
        " before ",
        " after ",
        " above ",
        " below ",
        " to ",
        " from ",
        " up ",
        " down ",
        " in ",
        " out ",
        " on ",
        " off ",
        " over ",
        " under ",
        " again ",
        " further ",
        " then ",
        " once ",
        " here ",
        " there ",
        " when ",
        " where ",
        " why ",
        " how ",
        " all ",
        " any ",
        " both ",
        " each ",
        " few ",
        " more ",
        " most ",
        " other ",
        " some ",
        " such ",
        " no ",
        " nor ",
        " not ",
        " only ",
        " own ",
        " same ",
        " so ",
        " than ",
        " too ",
        " very ",
        " s ",
        " t ",
        " can ",
        " will ",
        " just ",
        " don ",
        " don't ",
        " should ",
        " should've ",
        " now ",
        " d ",
        " ll ",
        " m ",
        " o ",
        " re ",
        " ve ",
        " y ",
        " ain ",
        " aren ",
        " aren't ",
        " couldn ",
        " couldn't ",
        " didn ",
        " didn't ",
        " doesn ",
        " doesn't ",
        " hadn ",
        " hadn't ",
        " hasn ",
        " hasn't ",
        " haven ",
        " haven't ",
        " isn ",
        " isn't ",
        " ma ",
        " mightn ",
        " mightn't ",
        " mustn ",
        " mustn't ",
        " needn ",
        " needn't ",
        " shan ",
        " shan't ",
        " shouldn ",
        " shouldn't ",
        " wasn ",
        " wasn't ",
        " weren ",
        " weren't ",
        " won ",
        " won't ",
        " wouldn ",
        " wouldn't ",
    )
}