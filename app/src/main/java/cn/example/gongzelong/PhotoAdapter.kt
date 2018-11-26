package cn.example.gongzelong

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.example.imageloader.ImageLoader2

class PhotoAdapter internal constructor(private val mContext: Context, private val list: List<PhotoInfo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val inflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mGetPhoto: GetPhoto? = null

    fun getPhoto(GetPhoto: GetPhoto) {
        this.mGetPhoto = GetPhoto
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(inflater.inflate(R.layout.item_photo_adapter, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val params = holder.itemView.layoutParams
        params.height = getScreenWidth(mContext) / 3
        params.width = getScreenWidth(mContext) / 3
        holder.itemView.layoutParams = params//重设item大小

        if (getItemViewType(position) == 0) {
            holder.itemView.setOnClickListener { v ->
                //因为拍照要返回参数，所以接口回调方式将事件处理交给activity处理
                mGetPhoto!!.getTakeCamera(v)
            }
            return
        }

        val myViewHolder = holder as MyViewHolder
        val photoInfo = list[position - 1]


//        val imageLoader = ImageLoader(holder.itemView.context)
//        imageLoader.displayImage(photoInfo.path, myViewHolder.image)

//        Glide.with(mContext).load(photoInfo.path).into(myViewHolder.image)

        val imageLoader2 = ImageLoader2(holder.itemView.context)
        imageLoader2.disPlay(photoInfo.path, myViewHolder.image)


    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            0
        } else 1
    }

    override fun getItemCount(): Int {
        return list.size
    }

    internal inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById<View>(R.id.image) as ImageView

    }

    /**
     * 获得屏幕高度
     * @param context context
     * @return 屏幕高度
     */
    private fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    //获取拍照后的照片
    interface GetPhoto {
        fun getTakeCamera(v: View)
    }
}