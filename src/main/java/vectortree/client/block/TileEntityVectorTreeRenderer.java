package vectortree.client.block;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;

public class TileEntityVectorTreeRenderer extends TileEntitySpecialRenderer
{
    public void renderTileEntityAt(TileEntity var1, double x, double y, double z, float var8) {
    	
    	GL11.glDisable(GL11.GL_CULL_FACE);
    	
    	renderIcon(x+1, y + 1.5, z, 16, 16, 0/*RenderManager.instance.playerViewY*/, Items.apple.getIconFromDamage(0));
    	
    	GL11.glEnable(GL11.GL_CULL_FACE);
    	
    }
    
    public void renderIcon(double par3, double par5, double par7, int width, int height, float angle, IIcon parIcon) {
    	

    	int ripeState = (int) ((System.currentTimeMillis() * 0.05D) % 255);
    	Color colorTint = new Color(ripeState, 255, ripeState);
    	float scale = 0.5F + 2.5F * ((float)ripeState / 255F);
    	double rotateAnimateRate = 0.1D;
    	double rotateAnimateRange = 0.2D;
    	double rotateAnimate = Math.toDegrees(Math.sin(Math.toRadians((System.currentTimeMillis() * rotateAnimateRate) % 360))) * rotateAnimateRange;
    	double rotateAnimate2 = Math.toDegrees(Math.sin(Math.toRadians(((System.currentTimeMillis() - 300) * rotateAnimateRate * 1.2D) % 360))) * rotateAnimateRange * 0.5D;
    	
        float var13 = 0.016666668F * scale;
        int borderSize = 2;
    	
    	GL11.glPushMatrix();
        GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
        
        GL11.glRotated(-rotateAnimate, 0.0F, 0.0F, 1.0F);
        GL11.glRotated(-rotateAnimate2, 1.0F, 0.0F, 0.0F);
        //GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
        
        GL11.glScalef(-var13, -var13, var13);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        Tessellator var14 = Tessellator.instance;
        byte var15 = 0;
        
        var14.startDrawingQuads();
        
        RenderManager.instance.renderEngine.bindTexture(TextureMap.locationItemsTexture);
        
        float f6 = parIcon.getMinU();
        float f7 = parIcon.getMaxU();
        float f9 = parIcon.getMinV();
        float f8 = parIcon.getMaxV();
        
        var14.setColorRGBA(colorTint.getRed(), colorTint.getGreen(), colorTint.getBlue(), 255);
        var14.addVertexWithUV((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D, f6, f9);
        var14.addVertexWithUV((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D, f6, f8);
        var14.addVertexWithUV((double)(width / 2 + borderSize), (double)(height + var15), 0.0D, f7, f8);
        var14.addVertexWithUV((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D, f7, f9);
        var14.draw();
        
        GL11.glDisable(GL11.GL_BLEND);
        
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, float angle)
    {
    	renderLivingLabel(par2Str, par3, par5, par7, par9, 200, 80, angle);
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height, float angle)
    {
        //float var10 = par1EntityLivingBase.getDistanceToEntity(this.renderManager.livingPlayer);

        int borderSize = 2;
        
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    	
        //if (var10 <= (float)par9)
        //{
            FontRenderer var11 = RenderManager.instance.getFontRenderer();
            float var12 = 0.6F;
            float var13 = 0.016666668F * var12;
            GL11.glPushMatrix();
            GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
            //GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-var13, -var13, var13);
            GL11.glDisable(GL11.GL_LIGHTING);
            
            if (par9 == 0) {
	            GL11.glDepthMask(false);
	            //GL11.glDisable(GL11.GL_DEPTH_TEST);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            Tessellator var14 = Tessellator.instance;
	            byte var15 = 0;
	            
	            //GL11.glDisable(GL11.GL_TEXTURE_2D);
	            var14.startDrawingQuads();
	            //int width = var11.getStringWidth(par2Str) / 2;
	            
	            /*RenderManager.instance.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
	            
	            Icon particleIcon = CommonProxy.blockWeatherDeflector.getBlockTextureFromSide(0);
	            
	            float f6 = particleIcon.getMinU();
	            float f7 = particleIcon.getMaxU();
	            float f8 = particleIcon.getMinV();
	            float f9 = particleIcon.getMaxV();*/
	            
	            /*var14.setColorRGBA_F(1F, 1F, 1F, 1F);
	            var14.addVertexWithUV((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D, f6, f9);
	            var14.addVertexWithUV((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D, f6, f8);
	            var14.addVertexWithUV((double)(width / 2 + borderSize), (double)(height + var15), 0.0D, f7, f8);
	            var14.addVertexWithUV((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D, f7, f9);*/
            
	            var14.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
	            var14.addVertex((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D);
	            var14.addVertex((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D);
	            var14.addVertex((double)(width / 2 + borderSize), (double)(height + var15), 0.0D);
	            var14.addVertex((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D);
	            var14.draw();
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            //var11.drawString(par2Str, -var11.getStringWidth(par2Str) / 2, var15, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            var11.drawString(par2Str, -width/2+borderSize/*-var11.getStringWidth(par2Str) / 2*/, 0, 0xFFFFFF);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        //}
            
            GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
