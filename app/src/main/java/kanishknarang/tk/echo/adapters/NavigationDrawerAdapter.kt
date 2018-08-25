package kanishknarang.tk.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import kanishknarang.tk.echo.R
import kanishknarang.tk.echo.activities.MainActivity
import kanishknarang.tk.echo.fragments.AboutUsFragment
import kanishknarang.tk.echo.fragments.FavouriteFragment
import kanishknarang.tk.echo.fragments.MainScreenFragment
import kanishknarang.tk.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList: ArrayList<String>,_getImages: IntArray,_context: Context):
        RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>(){

    var contentList:ArrayList<String>?=null
    var getImages: IntArray?=null
    var mContext: Context?=null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {

        var itemView =LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_navigationdrawer,parent,false)

        var returnThis = NavViewHolder(itemView)
        return returnThis

    }

    override fun getItemCount(): Int {
        return (contentList as ArrayList).size

    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {

        holder?.icon_GET?.setBackgroundResource(getImages?.get(position)as Int)
        holder?.text_GET?.setText(contentList?.get(position))
        holder?.contentHolder?.setOnClickListener({
            if(position==0){
                var mainScreenFragment = MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,mainScreenFragment,"mainScreenFragment")
                        .addToBackStack("mainScreenFragment")
                        .commit()
            }else if (position == 1){
                var favouriteFragment = FavouriteFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,favouriteFragment)
                        .addToBackStack("mainScreenFragment")
                        .commit()
            }else if (position == 2){
                var settingsFragment = SettingsFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,settingsFragment)
                        .addToBackStack("mainScreenFragment")
                        .commit()
            }else{
                var aboutUsFragment = AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment,aboutUsFragment)
                        .addToBackStack("mainScreenFragment")
                        .commit()
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        })
//test
    }

    class NavViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        var icon_GET: ImageView?=null
        var text_GET: TextView? =null
        var contentHolder: RelativeLayout?=null

        init {
            icon_GET = itemView?.findViewById(R.id.icon_navdrawer)
            text_GET = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.navdrawer_item_content_holder)
        }
    }
}