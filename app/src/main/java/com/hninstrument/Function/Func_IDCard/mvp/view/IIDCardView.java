package com.hninstrument.Function.Func_IDCard.mvp.view;
import android.graphics.Bitmap;
import cbdi.drv.card.ICardInfo;
/**
 * Created by zbsz on 2017/6/9.
 */
public interface IIDCardView {
    void onsetCardInfo(ICardInfo cardInfo);

    void onsetCardImg(Bitmap bmp);

    void onSetText(String Msg);
}
