package com.dxj.tool.controller;

import com.dxj.tool.service.AlipayService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import com.dxj.log.annotation.Log;
import com.dxj.tool.domain.AlipayConfig;
import com.dxj.tool.domain.vo.TradeVo;
import com.dxj.tool.util.AliPayStatusEnum;
import com.dxj.tool.util.AlipayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author dxj
 * @date 2019-04-30
 */
@Slf4j
@RestController
@RequestMapping("api")
public class AliPayController {

    private final AlipayUtils alipayUtils;

    private final AlipayService alipayService;

    @Autowired
    public AliPayController(AlipayUtils alipayUtils, AlipayService alipayService) {
        this.alipayUtils = alipayUtils;
        this.alipayService = alipayService;
    }

    @GetMapping(value = "/aliPay")
    public ResponseEntity<AlipayConfig> get() {
        return new ResponseEntity<>(alipayService.find(), HttpStatus.OK);
    }

    @Log("配置支付宝")
    @PutMapping(value = "/aliPay")
    public ResponseEntity<AlipayConfig> payConfig(@Validated @RequestBody AlipayConfig alipayConfig) {
        alipayConfig.setId(1L);
        alipayService.update(alipayConfig);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("支付宝PC网页支付")
    @ApiOperation(value = "PC网页支付")
    @PostMapping(value = "/aliPay/toPayAsPC")
    public ResponseEntity<String> toPayAsPC(@Validated @RequestBody TradeVo trade) throws Exception {
        log.warn("REST request to toPayAsPC Trade : {}" + trade);
        AlipayConfig alipay = alipayService.find();
        trade.setOutTradeNo(alipayUtils.getOrderCode());
        String payUrl = alipayService.toPayAsPC(alipay, trade);
        return ResponseEntity.ok(payUrl);
    }

    @Log("支付宝手机网页支付")
    @ApiOperation(value = "手机网页支付")
    @PostMapping(value = "/aliPay/toPayAsWeb")
    public ResponseEntity<String> toPayAsWeb(@Validated @RequestBody TradeVo trade) throws Exception {
        log.warn("REST request to toPayAsWeb Trade : {}" + trade);
        AlipayConfig alipay = alipayService.find();
        trade.setOutTradeNo(alipayUtils.getOrderCode());
        String payUrl = alipayService.toPayAsWeb(alipay, trade);
        return ResponseEntity.ok(payUrl);
    }

    @ApiIgnore
    @GetMapping("/aliPay/return")
    @ApiOperation(value = "支付之后跳转的链接")
    public ResponseEntity<String> returnPage(HttpServletRequest request, HttpServletResponse response) {
        AlipayConfig alipay = alipayService.find();
        response.setContentType("text/html;charset=" + alipay.getCharset());
        //内容验签，防止黑客篡改参数
        if (alipayUtils.rsaCheck(request, alipay)) {
            //商户订单号
            String outTradeNo = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //支付宝交易号
            String tradeNo = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            System.out.println("商户订单号" + outTradeNo + "  " + "第三方交易号" + tradeNo);

            /**
             * 根据业务需要返回数据，这里统一返回OK
             */
            return new ResponseEntity<>("payment successful", HttpStatus.OK);
        } else {
            /**
             * 根据业务需要返回数据
             */
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @ApiIgnore
    @RequestMapping("/aliPay/notify")
    @ApiOperation(value = "支付异步通知(要公网访问)，接收异步通知，检查通知内容app_id、out_trade_no、total_amount是否与请求中的一致，根据trade_status进行后续业务处理")
    public ResponseEntity<Void> notify(HttpServletRequest request) throws Exception {
        AlipayConfig alipay = alipayService.find();
        Map<String, String[]> parameterMap = request.getParameterMap();
        StringBuilder notifyBuild = new StringBuilder("/****************************** pay notify ******************************/\n");
        parameterMap.forEach((key, value) -> notifyBuild.append(key).append("=").append(value[0]).append("\n"));
        //内容验签，防止黑客篡改参数
        if (alipayUtils.rsaCheck(request, alipay)) {
            //交易状态
            String tradeStatus = new String(request.getParameter("trade_status").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            // 商户订单号
            String outTradeNo = new String(request.getParameter("out_trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //支付宝交易号
            String tradeNo = new String(request.getParameter("trade_no").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //付款金额
            String totalAmount = new String(request.getParameter("total_amount").getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            //验证
            if (tradeStatus.equals(AliPayStatusEnum.SUCCESS.getValue()) || tradeStatus.equals(AliPayStatusEnum.FINISHED.getValue())) {
                //验证通过后应该根据业务需要处理订单
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
