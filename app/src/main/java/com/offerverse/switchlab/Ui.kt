package com.offerverse.switchlab

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

@Composable
fun SwitchLabScreen() {
    val result = Calc.run(
        LocalDate.of(2025,10,20), LocalDate.of(2026,11,1), LocalDate.of(2029,11,1),
        110000.0, .76, .70, 18200.0, 13500.0, 170.0, 241.70, 7000.0, 3500.0, 0.0, 140000.0,
        listOf(120000.0,125000.0,130000.0,135000.0,140000.0,145000.0,150000.0)
    )
    Column(Modifier.fillMaxSize().background(Bg).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("OfferVerse SwitchLab", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Native job switch money estimator", color = Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("Stay total", money(result.stay), Modifier.weight(1f))
            Tile("Switch total", money(result.selectedSwitch), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile("Delta", signed(result.selectedSwitch-result.stay), Modifier.weight(1f), result.selectedSwitch >= result.stay)
            Tile("Break-even", money(result.breakEven), Modifier.weight(1f), true)
        }
        CardPanel("Salary what-if chart") { Bars(result.scenarios) }
        CardPanel("Scenario table") {
            result.scenarios.forEach {
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(money0(it.base), color = Txt, fontWeight = FontWeight.Bold)
                    Text(signed(it.delta), color = if (it.delta >= 0) Green else Red, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Prefilled with your Amazon to Cap One case. Next version will add editable inputs and save/export.", color = Muted, fontSize = 12.sp)
    }
}

@Composable fun CardPanel(title:String, content:@Composable ColumnScope.()->Unit)=Card(Modifier.fillMaxWidth(), shape=RoundedCornerShape(22.dp), colors=CardDefaults.cardColors(CardBg)){ Column(Modifier.padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp)){ Text(title,color=Txt,fontWeight=FontWeight.Bold); content() } }
@Composable fun Tile(label:String,value:String,mod:Modifier=Modifier,good:Boolean=false)=Card(mod,colors=CardDefaults.cardColors(if(good) Color(0xFF102A24) else Surface2),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(12.dp)){Text(label,color=Muted,fontSize=12.sp);Text(value,color=if(good) Green else Txt,fontSize=20.sp,fontWeight=FontWeight.Bold)}}
@Composable fun Bars(items:List<Scenario>){val maxAbs=items.maxOfOrNull{abs(it.delta)}?.coerceAtLeast(1.0)?:1.0;Canvas(Modifier.fillMaxWidth().height(150.dp)){val mid=size.height/2;drawLine(Muted, Offset(0f,mid), Offset(size.width,mid),2f);val w=size.width/(items.size*1.5f);items.forEachIndexed{idx,s->val h=(abs(s.delta)/maxAbs*(size.height*.42f)).toFloat();val x=idx*size.width/items.size+w*.25f;val y=if(s.delta>=0) mid-h else mid;drawRoundRect(if(s.delta>=0) Green else Red,Offset(x,y),Size(w,h),CornerRadius(8f,8f))}}}
fun money(v:Double)=NumberFormat.getCurrencyInstance(Locale.US).apply{maximumFractionDigits=0}.format(v)
fun money0(v:Double)="$"+String.format(Locale.US,"%,.0f",v)
fun signed(v:Double)=if(v>=0) "+${money(v)}" else "-${money(abs(v))}"
val Bg=Color(0xFF050713); val CardBg=Color(0xFF0D1324); val Surface2=Color(0xFF161C31); val Txt=Color(0xFFE5E7EB); val Muted=Color(0xFF9CA3AF); val Green=Color(0xFF22C55E); val Red=Color(0xFFEF4444)
