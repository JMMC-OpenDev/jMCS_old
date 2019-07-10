/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Several Image utility methods
 * 
 * @author Sylvain LAFRASSE.
 */
public final class ImageUtils {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(ImageUtils.class.getName());
    /* members */
    /** Loading error message template */
    private static final String CANNOT_LOAD_ICON_MESSAGE = "Could not load icon '{}'.";

    /**
     * Forbidden constructor
     */
    private ImageUtils() {
        // no-op
    }

    /**
     * Try to load the given resource path as ImageIcon.
     *
     * @param url the image icon resource path
     *
     * @return the retrieved image icon if found, null otherwise.
     */
    public static ImageIcon loadResourceIcon(final String url) {

        // TODO : Maybe cache previously loaded icon
        if (url == null) {
            _logger.debug(CANNOT_LOAD_ICON_MESSAGE, url);
            return null;
        }

        URL imageUrl;
        try {
            imageUrl = ResourceUtils.getResource(url);
        } catch (IllegalStateException e) {
            if (StringUtils.isEmpty(url)) {
                _logger.debug(CANNOT_LOAD_ICON_MESSAGE, url);
            } else {
                _logger.info(CANNOT_LOAD_ICON_MESSAGE, url);
            }
            return null;
        }

        imageUrl = UrlUtils.fixJarURL(imageUrl);
        _logger.debug("Using fixed URL '{}' for icon resource.", imageUrl);

        ImageIcon imageIcon = null;
        try {
            // Forge icon resource path
            imageIcon = new ImageIcon(imageUrl);
        } catch (IllegalStateException ise) {
            _logger.warn(CANNOT_LOAD_ICON_MESSAGE, imageUrl);
        }
        return imageIcon;
    }

    /**
     * Scales a given image to given maximum width and height.
     *
     * @param imageIcon the image to scale
     * @param maxHeight the maximum height of the scaled image, or automatic proportional scaling if less than or equal to 0
     * @param maxWidth the maximum width of the scaled image, or automatic proportional scaling if less than or equal to 0
     *
     * @return the scaled image
     */
    public static ImageIcon getScaledImageIcon(final ImageIcon imageIcon, final int maxHeight, final int maxWidth) {
        // Give up if params messed up
        if ((maxHeight == 0) && (maxWidth == 0)) {
            return imageIcon;
        }

        final int iconWidth = imageIcon.getIconWidth();
        final int iconHeight = imageIcon.getIconHeight();

        // If no resizing required
        if ((maxHeight >= iconHeight) && (maxWidth >= iconWidth)) {
            // Return early
            return imageIcon;
        }

        int newHeight = iconHeight;
        int newWidth = iconWidth;

        if (maxHeight > 0) {
            newHeight = Math.min(iconHeight, maxHeight);
            newWidth = (int) Math.ceil(((double) newHeight / (double) iconHeight) * iconWidth);
        }
        if (maxWidth > 0) {
            newWidth = Math.min(iconWidth, maxWidth);
            newHeight = (int) Math.ceil(((double) newWidth / (double) iconWidth) * iconHeight);
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Scaling image from {} x {} to {} x {}.",
                    iconWidth, iconHeight, newWidth, newHeight);
        }
        return new ImageIcon(imageIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING));
    }

    /**
     * Create a cross hatched texture
     * @param size internal image size (height = width)
     * @param backgroundColor background color
     * @param stripeColor line color
     * @param stroke line stroke
     * @return cross hatched texture paint
     */
    public static Paint createHatchedTexturePaint(final int size, final Color backgroundColor, final Color stripeColor, final Stroke stroke) {

        // create buffered image (alpha):
        final BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2d = bufferedImage.createGraphics();

        // LBO: use antialiasing:
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, size, size);

        g2d.setStroke(stroke);
        g2d.setColor(stripeColor);
        g2d.drawLine(0, 0, size, size);
        g2d.drawLine(0, size, size, 0);

        g2d.dispose();

        return new TexturePaint(bufferedImage, new Rectangle(0, 0, size, size));
    }
}
