package org.springframework.samples.petclinic.pdf;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import rst.pdfbox.layout.elements.Element;
import rst.pdfbox.layout.elements.Drawable;
import rst.pdfbox.layout.elements.Paragraph;
import rst.pdfbox.layout.text.BaseFont;
import rst.pdfbox.layout.text.DrawListener;
import rst.pdfbox.layout.text.Position;

public class LineBox implements Element, Drawable {

    private Paragraph inner;
    private float leftMargin;
    private float rightMargin;
    private float topMargin;
    private float bottomMargin;
    private Color backgroundColor;

    private float width;
    private float height;

    private Position absolutePosition;

    public LineBox(final Paragraph inner, final float width, final float height) {
      this.inner = inner;
      this.width = width;
      this.height = height;
    }

    public float getLeftMargin() {
    return leftMargin;
    }

    public void setLeftMargin(float leftMargin) {
    this.leftMargin = leftMargin;
    }

    public float getRightMargin() {
    return rightMargin;
    }

    public void setRightMargin(float rightMargin) {
    this.rightMargin = rightMargin;
    }

    public float getTopMargin() {
    return topMargin;
    }

    public void setTopMargin(float topMargin) {
    this.topMargin = topMargin;
    }

    public float getBottomMargin() {
    return bottomMargin;
    }

    public void setBottomMargin(float bottomMargin) {
    this.bottomMargin = bottomMargin;
    }

    public void setMargins(float left, float right, float top, float bottom) {
    setLeftMargin(left);
    setRightMargin(right);
    setTopMargin(top);
    setBottomMargin(bottom);
    }

    public Color getBackgroundColor() {
    return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
    }

    @Override
    public float getWidth() throws IOException {
    return width;
    }

    @Override
    public float getHeight() throws IOException {
    return height;
    }

    @Override
    public Position getAbsolutePosition() throws IOException {
    return absolutePosition;
    }


    public void setAbsolutePosition(Position absolutePosition) {
        this.absolutePosition = absolutePosition;
    }

    @Override
    public void draw(
        PDDocument pdDocument, 
        PDPageContentStream contentStream,
        Position upperLeft, 
        DrawListener drawListener) 
    throws IOException {
    
        float x = upperLeft.getX();
        float y = upperLeft.getY() - getHeight();
        contentStream.setNonStrokingColor(getBackgroundColor());
        contentStream.fillRect(x, y, getWidth(), getHeight());

        inner.setMaxWidth(getWidth() - getLeftMargin() - getRightMargin());
        Position innerUpperLeft = upperLeft
            .add(getLeftMargin(), -getTopMargin());
        inner.draw(pdDocument, contentStream, innerUpperLeft, drawListener);
    }

    @Override
    public Drawable removeLeadingEmptyVerticalSpace() throws IOException {
      return this;
    }

}
