import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vasensio.bluetooth_list_recyclerview.MainActivity
import com.vasensio.bluetooth_list_recyclerview.R

class CustomAdapter(
    private val dataSet: MutableList<MainActivity.Dispositivo>,
    private val onItemClick: (MainActivity.Dispositivo) -> Unit
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
        val tvMac: TextView = view.findViewById(R.id.tvMac)
    }

    override fun onCreateViewHolder(viewGroup: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_layout, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]

        viewHolder.tvTitulo.text = item.nombre
        viewHolder.tvMac.text = item.mac

        viewHolder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = dataSet.size
}