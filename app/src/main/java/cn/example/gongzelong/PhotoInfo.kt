package cn.example.gongzelong

class PhotoInfo(var path: String                 // 图片路径
                , var name: String                 // 图片名
                , var time: Long                   // 图片添加时间
                , var checked: Boolean?             //checkbox  选中状态
) {

    override fun toString(): String {
        return "PhotoInfo{" +
                "name='" + name + '\''.toString() +
                ", path='" + path + '\''.toString() +
                ", time=" + time +
                '}'.toString()
    }

    override fun equals(other: Any?): Boolean {
        try {
            @Suppress("NAME_SHADOWING")
            val other = other as PhotoInfo?
            return this.path.equals(other = other!!.path, ignoreCase = true)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + (checked?.hashCode() ?: 0)
        return result
    }
}