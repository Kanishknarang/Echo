package kanishknarang.tk.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import kanishknarang.tk.echo.R
import kanishknarang.tk.echo.Songs
import kanishknarang.tk.echo.adapters.FavouriteAdapter
import kanishknarang.tk.echo.databases.EchoDatabase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class FavouriteFragment : Fragment() {
    var myActivity: Activity?=null
    var noFavorites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int?=null
    var favoriteContent: EchoDatabase?=null
    var refreshList: ArrayList<Songs>?=null
    var getListFromDataBase: ArrayList<Songs>?=null

    object  Statified{
        var mediaPlayer: MediaPlayer?=null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourite, container, false)
        activity?.title = "Favorites"
        noFavorites = view?.findViewById(R.id.noFavourites)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view.findViewById(R.id.songTitle)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        recyclerView = view.findViewById(R.id.favouriteRecycler)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity=activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(myActivity)
        display_favorites_by_searching()
        bottomBarSetup()

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }
    fun getSongsFromPhone(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle =songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist =songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData =songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex =songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()){
                var currentId =songCursor.getLong(songId)
                var currentTitle =songCursor.getString(songTitle)
                var currentArtist=songCursor.getString(songArtist)
                var currentData=songCursor.getString(songData)
                var currentDate= songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId,currentTitle,currentArtist,currentData,currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup(){
        try{
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSonghelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener ({
                songTitle?.setText(SongPlayingFragment.Statified.currentSonghelper?.songTitle)
                SongPlayingFragment.Staticated.onSongCompletion()
            })
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                nowPlayingBottomBar?.visibility=View.VISIBLE
            }else{
                nowPlayingBottomBar?.visibility=View.INVISIBLE
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun bottomBarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener ({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            var songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putString("songArtist",SongPlayingFragment.Statified.currentSonghelper?.songArtist)
            args.putString("path",SongPlayingFragment.Statified.currentSonghelper?.songPath)
            args.putString("songTitle",SongPlayingFragment.Statified.currentSonghelper?.songTitle)
            args.putInt("songID",SongPlayingFragment.Statified.currentSonghelper?.songId?.toInt() as Int)
            args.putInt("songPosition",SongPlayingFragment.Statified.currentSonghelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData",SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar","success")
            songPlayingFragment.arguments=args
            fragmentManager?.beginTransaction()?.
                    replace(R.id.details_fragment,songPlayingFragment,"SongPlayingFragment")?.
                    addToBackStack("SongPlayingFragment")?.
                    commit()
        })

        playPauseButton?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition=SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)

            }else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition as Int)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun display_favorites_by_searching(){
        if (favoriteContent?.check_size() as Int >0){
            refreshList= ArrayList<Songs>()
            getListFromDataBase = favoriteContent?.queryDbList()
            val fetchListFromDevice = getSongsFromPhone()
            if (fetchListFromDevice!=null){
                for(i in 0..fetchListFromDevice?.size-1){
                    for (j in 0..getListFromDataBase?.size as Int - 1){
                        if (getListFromDataBase?.get(j)?.songID===fetchListFromDevice?.get(i)?.songID){
                            refreshList?.add((getListFromDataBase as ArrayList<Songs>)[j])
                        }
                    }
                }
            }else{}
            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            } else {
                /*Else we setup our recycler view for displaying the favorite songs*/
                val favoriteAdapter = FavouriteAdapter(refreshList as ArrayList<Songs>,
                        myActivity as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE

        }

    }




}
