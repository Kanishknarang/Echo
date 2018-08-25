package kanishknarang.tk.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.cleveroad.audiovisualization.VisualizerDbmHandler
import kanishknarang.tk.echo.CurrentSongHelper
import kanishknarang.tk.echo.R
import kanishknarang.tk.echo.Songs
import kanishknarang.tk.echo.databases.EchoDatabase
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.audioVisualization
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.currentPosition
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.currentSonghelper
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.endTimeText
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.fab
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.favouriteContent
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.fetchSongs
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.glView
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.loopImageButton
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.mSensorManager
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.mediaPlayer
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.myActivity
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.nextImageButton
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.playPauseImageButton
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.previousImageButton
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.seekbar
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.sensorListener
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.shuffleImageButton
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.songArtistView
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.songTitleView
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.startTimetext
import kanishknarang.tk.echo.fragments.SongPlayingFragment.Statified.updateSongsTIme
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object Statified{
        var myActivity: Activity?=null
        var mediaPlayer: MediaPlayer?=null
        var startTimetext: TextView?=null
        var endTimeText: TextView?=null
        var playPauseImageButton: ImageButton?=null
        var previousImageButton: ImageButton?=null
        var nextImageButton: ImageButton?=null
        var loopImageButton: ImageButton?=null
        var shuffleImageButton: ImageButton?=null
        var seekbar: SeekBar?=null
        var songArtistView: TextView?=null
        var songTitleView: TextView?=null
        var currentSonghelper: CurrentSongHelper?=null
        var currentPosition: Int?=null
        var fetchSongs: ArrayList<Songs>?=null
        var audioVisualization: AudioVisualization?=null
        var glView: GLAudioVisualizationView?=null
        var fab: ImageButton?=null
        var favouriteContent: EchoDatabase?=null
        var mSensorManager: SensorManager?=null
        var sensorListener: SensorEventListener?=null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongsTIme = object: Runnable{
            override fun run() {
                val getCurrent =mediaPlayer?.currentPosition
                startTimetext?.setText(String.format("%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long)-
                                TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))))
                seekbar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this,1000)
            }

        }

    }

    object Staticated{
        var MY_PREFS_SHUFFLE="shuffle feature"
        var MY_PREFS_LOOP="loop feature"

        fun onSongCompletion(){
            if (currentSonghelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
                currentSonghelper?.isPlaying=true
            }else{
                if (currentSonghelper?.isLoop as Boolean){
                    currentSonghelper?.isPlaying=true

                    var nextSong = fetchSongs?.get(currentPosition as Int)
                    currentSonghelper?.songPath = nextSong?.songData
                    currentSonghelper?.songTitle = nextSong?.songTitle
                    currentSonghelper?.songArtist = nextSong?.artist
                    currentSonghelper?.songId = nextSong?.songID as Long
                    mediaPlayer?.reset()
                    try {
                        mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSonghelper?.songPath))
                        mediaPlayer?.prepare()
                        mediaPlayer?.start()
                        processInformation(mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }else{
                    playNext("PlayNextNormal")
                    currentSonghelper?.isPlaying=true

                }
            }
            if (favouriteContent?.checkIfIdExists(currentSonghelper?.songId?.toInt() as Int) as Boolean){
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
            }else{
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songtitle: String,songArtist: String){
            var songTitleUpdated =songtitle
            var songArtistUpdated = songArtist
            if (songtitle.equals("<unknown>",true)){
                songTitleUpdated = "unknown"
            }
            if (songArtist.equals("<unknown>",true)){
                songArtistUpdated = "unknown"
            }
            songTitleView?.setText(songTitleUpdated)
            songArtistView?.setText(songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            /*Obtaining the final time*/
            val finalTime = mediaPlayer.duration
            /*Obtaining the current position*/
            val startTime = mediaPlayer.currentPosition
            /*Here we format the time and set it to the start time text*/
            seekbar?.max=finalTime
            startTimetext?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            /*Similar to above is done for the end time text*/
            endTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )
            /*Seekbar has been assigned this time so that it moves according to the time of
           song*/
            seekbar?.setProgress(startTime)
            /*Now this task is synced with the update song time obhect*/
            Handler().postDelayed(updateSongsTIme, 1000)
        }
        fun playNext(check: String) {
            /*Let this one sit for a while, We'll explain this after the next section where we
           will be teaching to add the next and previous functionality*/
            if (check.equals("PlayNextNormal", true)) {
                currentPosition = currentPosition!! + 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPosition
            }
            if (currentPosition == fetchSongs?.size) {
                currentPosition = 0
            }
            currentSonghelper?.isLoop=false
            var nextSong = fetchSongs?.get(currentPosition as Int)
            currentSonghelper?.songPath = nextSong?.songData
            currentSonghelper?.songTitle = nextSong?.songTitle
            currentSonghelper?.songArtist = nextSong?.artist
            currentSonghelper?.songId = nextSong?.songID as Long

            updateTextViews(currentSonghelper?.songTitle as String,currentSonghelper?.songArtist as String)
            mediaPlayer?.reset()
            try {
                mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSonghelper?.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                processInformation(mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (favouriteContent?.checkIfIdExists(currentSonghelper?.songId?.toInt() as Int) as Boolean){
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
            }else{
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect) as MenuItem
        item?.isVisible = true
        val item2: MenuItem? = menu.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect ->{
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        seekbar =view?.findViewById(R.id.seekBar)

        startTimetext=view?.findViewById(R.id.startTime)
        endTimeText=view?.findViewById(R.id.endTime)
        playPauseImageButton=view?.findViewById(R.id.playPauseButton)
        previousImageButton=view?.findViewById(R.id.previousButton)
        nextImageButton=view?.findViewById(R.id.nextButton)
        loopImageButton=view?.findViewById(R.id.loopButton)
        shuffleImageButton=view?.findViewById(R.id.shuffleButton)
        songArtistView=view?.findViewById(R.id.songArtist)
        songTitleView=view?.findViewById(R.id.songTitle)
        glView =view?.findViewById(R.id.visualizer_view)
        fab=view?.findViewById(R.id.favouiteIcon)
        fab?.alpha=0.8f

        return view
    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.sensorListener,Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        audioVisualization?.onPause()
        super.onPause()
        Statified.mSensorManager?.unregisterListener(sensorListener)
    }

    override fun onDestroy() {
        audioVisualization?.release()
        super.onDestroy()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var path: String?=null
        var _songTitle: String?=null
        var _songArtist: String?=null
        var _songId: Long=0
        favouriteContent = EchoDatabase(myActivity)

        currentSonghelper = CurrentSongHelper()
        currentSonghelper?.isPlaying=true
        currentSonghelper?.isLoop=false
        currentSonghelper?.isShuffle=false

        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            _songId = arguments?.getInt("songID")?.toLong()!!
            currentPosition= arguments?.getInt("songPosition")
            fetchSongs = arguments?.getParcelableArrayList("songData")

            currentSonghelper?.songPath =path
            currentSonghelper?.songArtist=_songArtist
            currentSonghelper?.songTitle=_songTitle
            currentSonghelper?.songId=_songId
            currentSonghelper?.currentPosition= Statified.currentPosition!!

            Staticated.updateTextViews(currentSonghelper?.songTitle as String,currentSonghelper?.songArtist as String)

        }catch (e: Exception){
            e.printStackTrace()
        }

        var fromFavFragment = arguments?.get("FavBottomBar") as? String

        if (fromFavFragment!=null){
            Statified.mediaPlayer = FavouriteFragment.Statified.mediaPlayer
        }else{
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaPlayer?.setDataSource(myActivity, Uri.parse(path))
                mediaPlayer?.prepare()
            }catch (e: Exception){
                e.printStackTrace()
            }
            mediaPlayer?.start()
        }

        Staticated.processInformation(mediaPlayer as MediaPlayer)
        if (currentSonghelper?.isPlaying as Boolean){
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{

            playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        mediaPlayer?.setOnCompletionListener {
            Staticated.onSongCompletion()
        }
        clickHandler()

        var visualizerHandler: VisualizerDbmHandler = DbmHandler.Factory.newVisualizerHandler(myActivity as Context,0)
        audioVisualization?.linkTo(visualizerHandler)

        var prefsForShuffle =myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isShuffleAllowed=prefsForShuffle?.getBoolean("feature",false)
        if (isShuffleAllowed as Boolean){
            currentSonghelper?.isShuffle=true
            currentSonghelper?.isLoop=false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }else{
            currentSonghelper?.isShuffle=false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop =myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)
        var isLoopAllowed=prefsForLoop?.getBoolean("feature",false)
        if (isLoopAllowed as Boolean){
            currentSonghelper?.isShuffle=false
            currentSonghelper?.isLoop=true
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }else{
            currentSonghelper?.isLoop=false
            shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        if (favouriteContent?.checkIfIdExists(currentSonghelper?.songId?.toInt() as Int) as Boolean){
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
        }else{
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
        }

    }
    fun clickHandler(){
        fab?.setOnClickListener({
            if (favouriteContent?.checkIfIdExists(currentSonghelper?.songId?.toInt() as Int) as Boolean){
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
                favouriteContent?.deleteFavourite(currentSonghelper?.songId?.toInt() as Int)
                Toast.makeText(myActivity,"Deleted from favourites",Toast.LENGTH_SHORT).show()
            }else{
                fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
                favouriteContent?.storeAsFavourite(currentSonghelper?.songId?.toInt() as Int,
                        currentSonghelper?.songArtist,
                        currentSonghelper?.songTitle,
                        currentSonghelper?.songPath)
                Toast.makeText(myActivity,"Added to Favourites",Toast.LENGTH_SHORT).show()
            }
        })
        shuffleImageButton?.setOnClickListener({
            var editorShuffle =myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop =myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()

            if(currentSonghelper?.isShuffle as Boolean){
                currentSonghelper?.isShuffle = false
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }else{
                currentSonghelper?.isShuffle=true
                currentSonghelper?.isLoop=false
                editorShuffle?.putBoolean("feature",true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }

        })
        nextImageButton?.setOnClickListener({
        currentSonghelper?.isPlaying=true
        playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if(currentSonghelper?.isShuffle as Boolean){
                Staticated.playNext("PlayNextLikeNormalShuffle")
            }else{
                Staticated.playNext("PlayNextNormal")
            }
        })
        previousImageButton?.setOnClickListener({
            currentSonghelper?.isPlaying=true
            if (currentSonghelper?.isLoop as Boolean){
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        loopImageButton?.setOnClickListener({
            var editorShuffle =myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop =myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()
            if(currentSonghelper?.isLoop as Boolean){
                currentSonghelper?.isLoop=false
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }else{
                currentSonghelper?.isLoop=true
                currentSonghelper?.isShuffle=false
                editorLoop?.putBoolean("feature",true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }

        })
        playPauseImageButton?.setOnClickListener({
            if (currentSonghelper?.isPlaying as Boolean){
                mediaPlayer?.pause()
                currentSonghelper?.isPlaying=false
                playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                mediaPlayer?.start()
                currentSonghelper?.isPlaying=true
                playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }



    fun playPrevious(){
        currentPosition = currentPosition!! -1
        if(currentPosition == -1){
            currentPosition=0
        }
        if(currentSonghelper?.isPlaying as Boolean){
            playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }else{
            playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSonghelper?.isLoop=false
        var nextSong = fetchSongs?.get(currentPosition as Int)
        currentSonghelper?.songPath = nextSong?.songData
        currentSonghelper?.songTitle = nextSong?.songTitle
        currentSonghelper?.songArtist = nextSong?.artist
        currentSonghelper?.songId = nextSong?.songID as Long

        Staticated.updateTextViews(currentSonghelper?.songTitle as String,currentSonghelper?.songArtist as String)

        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSonghelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            Staticated.processInformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (favouriteContent?.checkIfIdExists(currentSonghelper?.songId?.toInt() as Int) as Boolean){
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_on))
        }else{
            fab?.setImageDrawable(ContextCompat.getDrawable(myActivity as Context,R.drawable.favorite_off))
        }
    }

    fun bindShakeListener(){
        Statified.sensorListener = object : SensorEventListener{
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                val x= event.values[0]
                val y = event.values[1]
                val z=event.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x*x +y*y+z*z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration* 0.9f +delta

                if (mAcceleration>12){
                    val prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", false)
                    if (isAllowed as Boolean) {

                        Staticated.playNext("PlayNextNormal")
                    }
                }

            }

        }

    }


}
