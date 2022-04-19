package io.github.starwishsama.comet.genshin.utils

import io.github.starwishsama.comet.genshin.gacha.data.gacha.GachaResult
import io.github.starwishsama.comet.genshin.gacha.data.item.Item
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemStar
import io.github.starwishsama.comet.genshin.gacha.data.item.ItemType
import java.awt.*
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.random.nextInt


class DrawHelper {

    private val image: Image = FileUtils.getBackground()
    private val graphics: Graphics = image.graphics

    private val fontName = "HYWenHei"


    init {
        if (graphics is Graphics2D) {
            graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            )
            graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            )
        }
    }

    fun drawTenGachaImg(uid: Long, gachaResults: List<GachaResult>, inventory: List<GachaResult>): BufferedImage {
        val startIndexX = 230 + 151 * 9
        val startIndexY = 228
        var indexX = startIndexX
        val indexY = startIndexY
        gachaResults.sortedBy { it.item.itemStar.star }.forEach { result ->
            val count = inventory.count { it.item.id == result.item.id }
            drawFrame(indexX, indexY)
            drawShading(result.item, indexX, indexY)
            drawCharacterOrWeapon(result.item, indexX, indexY)
            drawLight(result.item, indexX)
            drawIcon(count, result.item, indexX, indexY)
            drawProspect(indexX, indexY)
            drawTrans(count, result.item, indexX, indexY)
            drawStar(count, result.item, indexX, indexY)
            drawNewIcon(count, indexX, indexY)
            drawCloseIcon()
            indexX -= 148
        }
        drawBubbles()
        drawWaterMark()
        drawUID(uid)
        graphics.dispose()
        return image.toBufferedImage()
    }

    fun drawGachaImg(uid: Long, gachaResult: GachaResult, inventory: List<GachaResult>): BufferedImage {
        val item = gachaResult.item
        val count = inventory.count { it.item.id == item.id }
        if (gachaResult.item.itemType == ItemType.CHARACTER) {
            drawCharacter(item)
            drawCharacterIcon(item)
            drawSingleName(item, 194, 633)
            drawCharacterStar(item)
            drawCharacterTrans(count, item)
        } else {
            drawWeaponBg(item)
            drawWeapon(item)
            drawWeaponIcon(item)
            drawSingleName(item, 190, 615)
            drawWeaponStar(item)
            drawWeaponTrans(item)
        }
        drawBubbles()
        drawWaterMark()
        drawUID(uid)
        graphics.dispose()
        return image.toBufferedImage()
    }

    private fun drawCharacter(item: Item) {
        val imgChar = FileUtils.getBigCharacter(item.itemName)
        val imgResize = imgChar.getScaledInstance(2688, 1344, Image.SCALE_SMOOTH)
        graphics.drawImage(imgResize, -339, -133, null)
    }

    private fun drawCharacterIcon(item: Item) {
        val imgIcon = FileUtils.getBigElement(item.itemSubType)
        graphics.drawImage(imgIcon, 73, 547, null)
    }


    private fun drawCharacterStar(item: Item) {
        val starCount = item.itemStar.star
        val starWidth = 34
        var indexX = 200
        val indexY = 663
        val imgStar = FileUtils.getStar()

        repeat(starCount){
            graphics.drawImage(imgStar, indexX, indexY, starWidth, starWidth, null)
            indexX += starWidth + 5
        }
    }

    private fun drawCharacterTrans(count: Int, item: Item) {
        if (item.itemType == ItemType.CHARACTER) {
            if (count in 2..7) {
                val imgStarLight = FileUtils.getStarLight(if (item.itemStar == ItemStar.FIVE) 10 else 2)
                graphics.drawImage(imgStarLight, 837, 917, null)
                val imgStarDust = FileUtils.getConstellationIcon(item.itemStar)
                graphics.drawImage(imgStarDust, 950, 917, null)
                graphics.font = Font(fontName, Font.PLAIN, 28)
                graphics.color = Color.WHITE
                graphics.drawString("重复角色，已转化", 842, 870)
            } else if (count > 7) {
                val imgStarLight = FileUtils.getStarLight(if (item.itemStar == ItemStar.FIVE) 25 else 5)
                graphics.drawImage(imgStarLight, 900, 917, null)
                graphics.font = Font(fontName, Font.PLAIN, 28)
                graphics.color = Color.WHITE
                graphics.drawString("重复角色，已转化", 842, 870)
            }
        }
    }


    private fun drawWeaponBg(item: Item) {
        val imgBg = FileUtils.getWeaponBackgroundIcon(item.itemSubType)
        graphics.drawImage(imgBg, 449, -17, 1114, 1114, null)
    }

    private fun drawWeapon(item: Item) {
        val imgWeapon = FileUtils.getWeapon(item.itemName)
        graphics.drawImage(imgWeapon, 699, -75, 614, 1230, null)
    }

    private fun drawWeaponIcon(item: Item) {
        val imgIcon = FileUtils.getBlackWeaponIcon(item.itemSubType)
        graphics.drawImage(imgIcon, 47, 512, null)
    }

    private fun drawSingleName(item: Item, x: Int, y: Int) {
        if (graphics is Graphics2D) {
            val font = Font(fontName, Font.BOLD, 60)
            val text = item.itemName
            graphics.font = font
            val outlineColor = Color.BLACK
            val fillColor = Color.WHITE
            val outlineStroke = BasicStroke(2.0f)

            // create a glyph vector from your text
            val glyphVector: GlyphVector = font.createGlyphVector(graphics.fontRenderContext, text)
            // get the shape object
            val originAf = graphics.transform
            val at = AffineTransform()
            at.translate(x.toDouble(), y.toDouble())
            graphics.transform = at

            val textShape = glyphVector.outline

            graphics.color = outlineColor
            graphics.stroke = outlineStroke
            graphics.draw(textShape) // draw outline


            graphics.color = fillColor
            graphics.fill(textShape) // fill the shape
            graphics.transform = originAf

        }
    }

    private fun drawWeaponStar(item: Item) {
        val starCount = item.itemStar.star
        val starWidth = 34
        var indexX = 190
        val indexY = 643
        val imgStar = FileUtils.getStar()

        repeat(starCount){
            graphics.drawImage(imgStar, indexX, indexY, starWidth, starWidth, null)
            indexX += starWidth + 5
        }
    }

    private fun drawWeaponTrans(item: Item) {
        val imgStarDust = FileUtils.getStarDust(item.itemStar)
        graphics.drawImage(imgStarDust, 1394, 485, null)
    }

    private fun drawBubbles() {
        val randBigCount = Random.nextInt(5..10)
        val bigImageList = FileUtils.getBigBubbles()
        repeat(randBigCount) {
            val randomWidth = Random.nextInt(50..200)
            val randomXIndex = Random.nextInt(20..1900)
            val randomYIndex = Random.nextInt(20..1060)
            val randomImage = bigImageList.random()
            graphics.drawImage(randomImage, randomXIndex, randomYIndex, randomWidth, randomWidth, null)
        }
        val randSmallCount = Random.nextInt(50..101)
        val smallImageList = FileUtils.getSmallBubbles()
        repeat(randSmallCount) {
            val randomWidth = Random.nextInt(5..15)
            val randomXIndex = Random.nextInt(20..1900)
            val randomYIndex = Random.nextInt(20..1060)
            val randomImage = smallImageList.random()
            graphics.drawImage(randomImage, randomXIndex, randomYIndex, randomWidth, randomWidth, null)
        }
    }

    private fun drawFrame(indexX: Int, indexY: Int) {
        val imgFrame = FileUtils.getFrame()
        graphics.drawImage(imgFrame, indexX, indexY, null)
    }

    private fun drawProspect(indexX: Int, indexY: Int) {
        val imgStarBg = FileUtils.getStarBackground()
        graphics.drawImage(imgStarBg, indexX + 9, indexY + 8, null)
    }

    private fun drawCharacterOrWeapon(item: Item, indexX: Int, indexY: Int) {
        if (item.itemType == ItemType.CHARACTER) {
            val imgChar = FileUtils.getSmallCharacter(item.itemName)
            val imgResize = imgChar.getScaledInstance(imgChar.getWidth(null), imgChar.getHeight(null), Image.SCALE_SMOOTH)
//            graphics.drawImage(imgChar, indexX - 1, indexY + 5, Rectangle(-3, 0, imgResize.getWidth(null), imgResize.getHeight(null)), GraphicsUnit.Pixel)
            graphics.drawImage(imgResize, indexX - 1, indexY + 5, null)
        } else {
            val imgWeapon = FileUtils.getWeapon(item.itemName)
            var imgResize = imgWeapon.getScaledInstance(305, 610, Image.SCALE_SMOOTH)
            imgResize = imgResize.toBufferedImage().getSubimage(80, 0, 140, 550)
//            graphics.drawImage(imgChar, indexX - 1, indexY + 5, Rectangle(80, 0, 140, 550), GraphicsUnit.Pixel)
            graphics.drawImage(imgResize, indexX + 5, indexY, null)

        }
    }

    private fun drawIcon(count: Int, item: Item, indexX: Int, indexY: Int) {
        if (item.itemType == ItemType.WEAPON) {
            val imgIcon = FileUtils.getWhiteWeaponIcon(item.itemSubType)
            graphics.drawImage(imgIcon, indexX + 30, indexY + 430, 100, 100, null)
        } else if (item.itemType == ItemType.CHARACTER && count == 1) {
            val imgIcon = FileUtils.getSmallElement(item.itemSubType)
            graphics.drawImage(imgIcon, indexX + 40, indexY + 440, 72, 72, null)
        }
    }

    private fun drawTrans(count: Int, item: Item, indexX: Int, indexY: Int) {
        if (item.itemType == ItemType.CHARACTER) {
            if (count in 2..7) {
                val imgStarLight = FileUtils.getStarLight(if (item.itemStar == ItemStar.FIVE) 10 else 2)
                graphics.drawImage(
                    imgStarLight,
                    indexX + 25,
                    indexY + 323,
                    null
                )
                val imgStarDust = FileUtils.getConstellationIcon(item.itemStar)
                graphics.drawImage(
                    imgStarDust,
                    indexX + 25,
                    indexY + 443,
                    null
                )
                graphics.font = Font(fontName, Font.PLAIN, 21)
                graphics.color = Color.WHITE
                graphics.drawString("转化", indexX + 57, indexY + 660)
            } else if (count > 7) {
                val imgStarLight = FileUtils.getStarLight(if (item.itemStar == ItemStar.FIVE) 25 else 5)
                graphics.drawImage(
                    imgStarLight,
                    indexX + 25,
                    indexY + 443,
                    null
                )
                graphics.font = Font(fontName, Font.PLAIN, 21)
                graphics.color = Color.WHITE
                graphics.drawString("转化", indexX + 57, indexY + 660)
            }
        }
    }

    private fun drawLight(item: Item, indexX: Int) {
        val shiftXIndex = when (item.itemStar) {
            ItemStar.FIVE -> -105
            ItemStar.FOUR -> -98
            ItemStar.THREE -> 2
        }
        val imgLight = FileUtils.getLight(item.itemStar)
        graphics.drawImage(imgLight, indexX + shiftXIndex, -5, null)
    }

    private fun drawStar(count: Int, item: Item, indexX: Int, indexY: Int) {

        if (count != 1 && item.itemType == ItemType.CHARACTER) return // 转化不用画星级

        val starCount = item.itemStar.star

        val starWidth = 21

        var indexXAdd = (155 - (starCount * starWidth))/2
        val imgStar = FileUtils.getStar()

        repeat(starCount) {
            graphics.drawImage(imgStar, indexX + indexXAdd, indexY + 535, starWidth, starWidth, null)
            indexXAdd += starWidth
        }
    }


    private fun drawShading(item: Item, indexX: Int, indexY: Int) {
        if (item.itemStar == ItemStar.FIVE) {
            val imgShading = FileUtils.getShading()
            graphics.drawImage(imgShading, indexX, indexY + 45, null)
        }
    }

    private fun drawCloseIcon() {
        val imgClose = FileUtils.getCloseIcon()
        graphics.drawImage(imgClose, 1920 - 105, 20, null)
    }

    private fun drawNewIcon(count: Int, indexX: Int, indexY: Int) {
        if (count != 1) return
        val imgNew = FileUtils.getNewIcon()
        graphics.drawImage(imgNew, indexX + 100, indexY + 20, null)
    }

    private fun drawWaterMark() {
        graphics.font = Font(fontName, Font.PLAIN, 19)
        graphics.color = Color.WHITE
        graphics.drawString("Generated by GacheSimulator/SDLMoe", 10, 1050)
    }

    private fun drawUID(uid: Long) {
        graphics.font = Font(fontName, Font.PLAIN, 21)
        graphics.color = Color.WHITE
        graphics.drawString("UID: $uid", 1650, 1042)
    }

    companion object {

        private fun Image.saveToPNG(path: String) {
            val bi = this.toBufferedImage()
            ImageIO.write(bi, "PNG", File(path))
        }

        private fun Image.toBufferedImage(): BufferedImage {
            // Create a buffered image with transparency
            val bimage = BufferedImage(this.getWidth(null), this.getHeight(null), BufferedImage.TYPE_INT_ARGB)

            // Draw the image on to the buffered image
            val bGr = bimage.createGraphics()
            bGr.drawImage(this, 0, 0, null)
            bGr.dispose()

            // Return the buffered image
            return bimage
        }

    }


}