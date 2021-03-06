package kanishknarang.tk.echo

import android.os.Parcel
import android.os.Parcelable

class Songs(var songID: Long,var songTitle: String,var artist: String,var songData: String,var dateAdded: Long): Parcelable{
    override fun writeToParcel(dest: Parcel?, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }
    object Statified{
    var nameComarator: Comparator<Songs> = Comparator<Songs>{song1,song2->
        val songone =song1.songTitle.toUpperCase()
        val songtwo =song2.songTitle.toUpperCase()
        songone.compareTo(songtwo)
    }
    var dateComparator: Comparator<Songs> = Comparator<Songs>{song1,song2->
        val songone = song1.dateAdded.toDouble()
        val songtwo = song2.dateAdded.toDouble()
        songtwo.compareTo(songone)
    }
}

}