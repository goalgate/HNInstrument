package com.hninstrument.Function.Func_IDCard.mvp.module;
import android.graphics.Bitmap;
import android.util.Log;
import com.hninstrument.AppInit;
import cbdi.drv.card.CardInfo;
import cbdi.drv.card.CardInfoRk123x;
import cbdi.drv.card.ICardInfo;
import cbdi.drv.card.ICardState;
import cbdi.log.Lg;

/**
 * Created by zbsz on 2017/6/4.
 */

public class IDCardImpl implements IIDCard {
    private static final String TAG = "信息提示";
    private int cdevfd = -1;
    private static ICardInfo cardInfo = null;
    IIdCardListener mylistener;
    @Override
    public void onOpen(IIdCardListener listener) {
        mylistener = listener;
        if (AppInit.getMyManager().getAndroidDisplay().startsWith("rk3368")){
            try {
                //cardInfo =new CardInfo("/dev/ttyAMA2",m_onCardState);
                cardInfo = new CardInfo("/dev/ttyS0", m_onCardState);
                cardInfo.setDevType("rk3368");
                cdevfd = cardInfo.open();
                if (cdevfd >= 0) {
                    Log.e(TAG, "打开身份证读卡器成功");
                } else {
                    cdevfd = -1;
                    Log.e(TAG, "打开身份证读卡器失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 1, AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 9)) >= 20180903
                &&Integer.parseInt(AppInit.getMyManager().getAndroidDisplay().substring(AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 1, AppInit.getMyManager().getAndroidDisplay().indexOf(".20") + 9)) < 20180918) {
            try {
                cardInfo = new CardInfoRk123x("/dev/ttyS0", m_onCardState);
                cardInfo.setDevType("rk3368");
                cdevfd = cardInfo.open();
                if (cdevfd >= 0) {
                    Log.e(TAG, "打开身份证读卡器成功");
                } else {
                    cdevfd = -1;
                    Log.e(TAG, "打开身份证读卡器失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                cardInfo = new CardInfoRk123x("/dev/ttyS1", m_onCardState);
                cardInfo.setDevType("rk3368");
                cdevfd = cardInfo.open();
                if (cdevfd >= 0) {
                    Log.e(TAG, "打开身份证读卡器成功");
                } else {
                    cdevfd = -1;
                    Log.e(TAG, "打开身份证读卡器失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReadCard() {
        cardInfo.readCard();
    }

    @Override
    public void onStopReadCard() {
        cardInfo.stopReadCard();
    }

    private ICardState m_onCardState = new ICardState() {
        @Override
        public void onCardState(int itype, int value) {
            if (itype == 4 && value == 1) {
                mylistener.onSetInfo(cardInfo);
                Bitmap bmp = cardInfo.getBmp();
                if (bmp != null) {
                    mylistener.onSetImg(bmp);
                } else {
                    Lg.e("信息提示", "没有照片");
                }
                cardInfo.clearIsReadOk();
            }

        }

    };

    @Override
    public void onClose() {
        cardInfo.close();
    }
}
