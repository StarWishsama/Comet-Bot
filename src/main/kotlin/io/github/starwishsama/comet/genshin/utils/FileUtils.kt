package io.github.starwishsama.comet.genshin.utils

import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemSubType
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO

object FileUtils {

    fun getLocalDir(): File = File(System.getProperty("user.dir"))

    fun getItemPool(): File = File( "${getLocalDir().path}/itemPool.json")

    fun getGachaPool(): File = File( "${getLocalDir().path}/gachaPools.json")

    fun getGachaCache(uid: Long): File = File( "${getLocalDir().path}/gachaCache/${uid}.json")

    fun getImageResources(): File = File(getLocalDir().path  + "/img")

    fun getBackground(): Image = ImageIO.read(File(getImageResources().path + "/背景/背景.png"))

    fun getFrame(): Image = ImageIO.read(File(getImageResources().path + "/框/框.png"))

    fun getStarBackground(): Image = ImageIO.read(File(getImageResources().path + "/框/星星.png"))

    fun getSmallCharacter(name: String): Image = ImageIO.read(File(getImageResources().path + "/角色小图/$name.png"))

    fun getBigCharacter(name: String): Image = ImageIO.read(File(getImageResources().path + "/角色大图/$name.png"))

    fun getWeapon(name: String): Image = ImageIO.read(File(getImageResources().path + "/武器/$name.png"))

    fun getLight(star: ItemStar): Image = ImageIO.read(File((getImageResources().path + "/框/${star.name}").getRandomPathInFolder()))

    fun getStar(): Image = ImageIO.read(File(getImageResources().path + "/图标/星星.png"))

    fun getBigElement(itemSubType: ItemSubType): Image = ImageIO.read(File(getImageResources().path + "/元素图标大/${itemSubType.name}.png"))

    fun getSmallElement(itemSubType: ItemSubType): Image = ImageIO.read(File(getImageResources().path + "/元素图标小/${itemSubType.name}.png"))

    fun getWhiteWeaponIcon(itemSubType: ItemSubType): Image = ImageIO.read(File(getImageResources().path + "/武器图标白/${itemSubType.name}.png"))

    fun getBlackWeaponIcon(itemSubType: ItemSubType): Image = ImageIO.read(File(getImageResources().path + "/武器图标黑/${itemSubType.name}.png"))

    fun getCloseIcon(): Image = ImageIO.read(File(getImageResources().path + "/图标/关闭.png"))

    fun getNewIcon(): Image = ImageIO.read(File(getImageResources().path + "/图标/new.png"))

    fun getConstellationIcon(itemStar: ItemStar): Image = ImageIO.read(File(getImageResources().path + "/图标/${itemStar.name}.png"))

    fun getStarLight(count: Int): Image = ImageIO.read(File(getImageResources().path + "/图标/星辉$count.png"))

    fun getStarDust(itemStar: ItemStar): Image = ImageIO.read(File(getImageResources().path + "/框/${itemStar.name}.png"))

    fun getWeaponBackgroundIcon(itemSubType: ItemSubType): Image = ImageIO.read(File(getImageResources().path + "/武器背景/${itemSubType.name}.png"))

    fun getShading(): Image = ImageIO.read(File(getImageResources().path + "/框/花纹.png"))

    fun getSmallBubbles(): List<Image> = File(getImageResources().path + "/泡泡/small").listFiles()?.fold(arrayListOf()) {acc, file -> acc.add(ImageIO.read(File(file.path))); acc} ?: arrayListOf()

    fun getBigBubbles(): List<Image> = File(getImageResources().path + "/泡泡/big").listFiles()?.fold(arrayListOf()) {acc, file -> acc.add(ImageIO.read(File(file.path))); acc} ?: arrayListOf()



    private fun String.getRandomPathInFolder(): String = (this + "/" + File(this).list()?.random())

}