package com.scb.efbs2.web.bean.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import com.scb.efbs2.dealsheet.DealSheetConstants;
import com.scb.efbs2.dealsheet.model.DaSortOrderModel;
import com.scb.efbs2.dealsheet.service.ProductSortOrderService;
import com.scb.efbs2.dealsheet.web.util.StaticDataUtil;
import com.scb.efbs2.exception.common.BusinessException;
import com.scb.efbs2.exception.common.ExceptionErrorCodes;
import com.scb.efbs2.model.common.BasePAModel;
import com.scb.efbs2.model.staticData.AfpModel;
import com.scb.efbs2.model.staticData.StaticDataSetupModel;
import com.scb.efbs2.model.transactional.BillingProductModel;
import com.scb.efbs2.model.transactional.ChargeCodeModel;
import com.scb.efbs2.model.transactional.ChargeCodeMulLangModel;
import com.scb.efbs2.model.transactional.ChargeCodeMulLangPending;
import com.scb.efbs2.model.transactional.ChargeCodePendingModel;
import com.scb.efbs2.model.transactional.ChargeMethodModel;
import com.scb.efbs2.model.transactional.ChargeMethodPendingModel;
import com.scb.efbs2.model.transactional.ChargePartnerSharingModel;
import com.scb.efbs2.model.transactional.EnableDisbaleSetupModel;
import com.scb.efbs2.model.transactional.PartnerSharingPendingModel;
import com.scb.efbs2.model.transactional.ProductChargeOnModel;
import com.scb.efbs2.model.transactional.ProductChargeOnPendingModel;
import com.scb.efbs2.model.transactional.ProductInvoiceOthLangDesModel;
import com.scb.efbs2.model.transactional.ProductInvoiceOthLangDesPending;
import com.scb.efbs2.model.transactional.ProductLovModel;
import com.scb.efbs2.model.transactional.ProductLovPendingModel;
import com.scb.efbs2.model.transactional.ProductModel;
import com.scb.efbs2.model.transactional.ProductModelPK;
import com.scb.efbs2.model.transactional.ProductPLAccountModel;
import com.scb.efbs2.model.transactional.ProductPLAccountPendingModel;
import com.scb.efbs2.model.transactional.ProductPendingModel;
import com.scb.efbs2.model.transactional.RulesModel;
import com.scb.efbs2.model.transactional.RulesPendingModel;
import com.scb.efbs2.service.product.ProductChargeService;
import com.scb.efbs2.service.product.ProductDefinitionService;
import com.scb.efbs2.service.product.RuleService;
import com.scb.efbs2.util.BaseConstants;
import com.scb.efbs2.util.MessageConstants;
import com.scb.efbs2.util.PricingConstants;
import com.scb.efbs2.util.PricingConstants.ChargeMethod;
import com.scb.efbs2.util.RulesConstants.RuleCategory;
import com.scb.efbs2.util.StaticDataCacheLoader;
import com.scb.efbs2.vo.common.CheckerMakerVO;
import com.scb.efbs2.vo.product.GlobalProductChargeCheckVO;
import com.scb.efbs2.vo.product.GlobalProductMappingVO;
import com.scb.efbs2.vo.product.ProductDefinitionVO;
import com.scb.efbs2.vo.product.SubProductVO;
import com.scb.efbs2.web.bean.BaseActionBean;
import com.scb.efbs2.web.bean.common.IGlobalSaveActionBean;
import com.scb.efbs2.web.context.security.UserContext;
import com.scb.efbs2.web.util.CartesianTable;
import com.scb.efbs2.web.util.FacesUtil;
import com.scb.efbs2.web.util.ValidatorUtil;
import com.scb.efbs2.web.util.staticData.AfpUtil;
import com.scb.efbs2.web.util.staticData.ChargeParamUtil;
import com.scb.efbs2.web.util.staticData.FunctionUtil;
import com.scb.gpbsplus.model.GlobalGpbsProductModel;
import com.scb.gpbsplus.model.GlobalGpbsProductPendingModel;

/**
 * Copyright notice: Copyright (c) 2008 ISCM, Scope International. All rights
 * reserved.
 * 
 * @author : Soon Yik Date: Jun 6, 2008 Time: 7:29:45 PM
 * 
 *         $Id: ProductDefinitionActionBean.java 12506 2015-05-21 03:00:57Z
 *         imran $ HISTORY ------- Tag Name Date (DD-MM-YYYY) Description
 *         ------------------------------------------------------------------------------------------------------------------------
 *         PBI000000193850 Ram(1563635) 05-02-2018 Rule type must be BASIC for
 *         Volume Products (NON MANUAL Interface) GPBS-394 Ram(1563635)
 *         12-06-2018 Stability Requirement : System Pricing Sheet validations -
 *         Rules validations
 */
public class ProductDefinitionActionBean extends BaseActionBean implements IGlobalSaveActionBean {
	private static final long serialVersionUID = 1L;
	private ProductDefinitionService productDefinitionService;
	private ProductSortOrderService productSortOrderService;
	private ProductChargeService productChargeService;
	private RuleService ruleService;
	private UserContext userContext = (UserContext) getSession(false).getAttribute(USER_CONTEXT);

	public void setProductDefinitionService(ProductDefinitionService productDefinitionService) {
		this.productDefinitionService = productDefinitionService;
	}

	public void setProductSortOrderService(ProductSortOrderService productSortOrderService) {
		this.productSortOrderService = productSortOrderService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public String doNew() throws Exception {
		super.removeManagedBean(ProductDefinitionBean.BACKING_BEAN_NAME);
		super.removeManagedBean(RuleBean.BACKING_BEAN_NAME);
		super.removeManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		super.removeManagedBean(MethodBean.BACKING_BEAN_NAME);
		super.removeManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		// Product Definition Setup.
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("formBean=" + formBean);
		}
		formBean.setMaintenanceStep(MaintenanceStep.New.value());
		formBean.resetSelectItems();
		// formBean.populateSearchInterfaceSelect();
		formBean.reset();
		//// Added by Srikanth For 4.4.1 multi language support -start
		formBean.setSubMethodList(null);
		formBean.setDeleteMethodList(null);
		formBean.clearMethodDetails();
		// ended
		// formBean.populateInterfaceSelect(formBean.getCountryCode());
		// Rule Setup
		RuleBean ruleForm = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
		/* ruleForm.resetRuleButton(); */
		ruleForm.resetRuleDetails();
		ruleForm.setListValues();
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = [" + (ruleForm != null ? ruleForm.toString() : null) + "]");
			log.debug("end: formBean = " + formBean);
		}
		// Product charge setup?
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		chargeBean.resetChargeCodeDetails();
		chargeBean.setChargeCodeList(null);
		chargeBean.setChargeCodeDeletedList(null);
		// Added by Srikanth For 4.4.1 multi language support -start
		chargeBean.setSubChargeInvList(null);
		chargeBean.setDeleteChargeInvList(null);
		chargeBean.clearMethodDetails();
		// end

		// partner sharing
		chargeBean.setPartnerShareList(null);
		chargeBean.setDeletePartnerList(null);
		chargeBean.clearPartnerMethodDetails();
		EnableDisbaleSetupModel enableDisbaleSetupModel = productDefinitionService.getEnableDisableSetupModel(
				formBean.getCountryCode(), BaseConstants.DEFAULT_BUSINESS_SEGMENT, BaseConstants.PARTNER_SHARING);
		if (enableDisbaleSetupModel != null && enableDisbaleSetupModel.getEnable_Disable_Flg() != null
				&& enableDisbaleSetupModel.getEnable_Disable_Flg().equalsIgnoreCase(BaseConstants.ENABLE_FLAG)) {
			chargeBean.setIsPartnerSharingEnabled(Boolean.TRUE);
		}
		// chargeBean.setIsPartnerSharingEnabled(Boolean.FALSE);
		// StaticDataUtil.partnerCahcheMap.clear();

		MethodBean methodForm = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		methodForm.resetMethodDetails();
		methodForm.setSubMethodList(null);
		methodForm.setDeleteMethodList(null);

		PandLSetupBean pandLSetupForm = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		pandLSetupForm.resetPLAccountDetails();
		pandLSetupForm.setSubPLAccountList(null);
		pandLSetupForm.setDeletePLAccountList(null);

		// SRIKANTH
		StaticDataUtil.languageCahcheMap.clear();
		StaticDataUtil.partnerCahcheMap.clear();
		StaticDataUtil.globalProdCahcheMap.clear();
		StaticDataUtil.prodPLAccountCacheMap.clear();

		return null;
	}

	/*
	 * public List<LovAttributeBean> compareLovList(List<LovAttributeBean>
	 * defaultList, List<LovAttributeBean> selectedList){ List<LovAttributeBean>
	 * tempList = new ArrayList<LovAttributeBean>(); Collection<LovAttributeBean> s2
	 * = CollectionUtils.subtract(defaultList, selectedList); for (LovAttributeBean
	 * v : s2){ tempList.add(v); } return tempList; } public List<LovAttributeBean>
	 * intersectionLovList(List<LovAttributeBean> defaultList,
	 * List<LovAttributeBean> selectedList){ List<LovAttributeBean> tempList = new
	 * ArrayList<LovAttributeBean>(); Collection<LovAttributeBean> s2 =
	 * CollectionUtils.intersection(defaultList, selectedList); for
	 * (LovAttributeBean v : s2){ tempList.add(v); } return tempList; }
	 */

	public String validateLovSelect(ActionEvent event) {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);

		if (log.isDebugEnabled()) {
			// log.debug("getSelectedLovAttribute.size() = " +
			// formBean.getSelectedLovAttribute().size());
			log.debug("getSelectedLovAttribute.size() = " + formBean.getSelectedLOVAttributesList().size());
		}

		// to check whether sub product exist or not
		// if yes not allow to deselect the lov list
		// validate charge method
		ProductChargeBean productChargeBean = (ProductChargeBean) super.getManagedBean(
				ProductChargeBean.BACKING_BEAN_NAME);
		boolean valid = true;
		String method = "";
		HashMap<String, String> pc = new HashMap<String, String>();
		if (productChargeBean.getChargeCodeList() != null) {
			for (ChargeCodePendingModel chargePending : productChargeBean.getChargeCodeList()) {
				if (chargePending.getStatus() != Status.DELETE_DRAFT.value()
						&& chargePending.getStatus() != Status.DELETE_REJECT.value()
						&& chargePending.getStatus() != Status.DELETE_SUBMIT.value()) {
					if (log.isDebugEnabled()) {
						log.debug("formBean.getProductCode()= " + formBean.getProductCode());
						log.debug("methodPending.getProductCode()= " + chargePending.getProductCode());
					}
					if (!formBean.getProductCode().equals(chargePending.getProductCode())) {
						valid = false;
						if (!pc.containsKey(chargePending.getProductCode())) {
							method = method + chargePending.getProductCode() + ", ";
							pc.put(chargePending.getProductCode(), chargePending.getProductCode());
						}
					}
				}
			}
			pc = null;
			if (!valid) {
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DESELECT_LOV,
						new Object[] { "Charge Code", method.trim().substring(0, method.length() - 1) });
				return null;
			}
		}

		// to check whether sub product exist or not
		// if yes not allow to deselect the lov list
		// validate charge method
		MethodBean productMethodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		if (productMethodBean.getSubMethodList() != null) {
			valid = true;
			method = "";
			pc = new HashMap<String, String>();
			for (ChargeMethodPendingModel methodPending : productMethodBean.getSubMethodList()) {
				if (methodPending.getStatus() == null || (methodPending.getStatus() != null
						&& (methodPending.getStatus() != Status.DELETE_DRAFT.value()
								&& methodPending.getStatus() != Status.DELETE_REJECT.value()
								&& methodPending.getStatus() != Status.DELETE_SUBMIT.value()))) {
					if (log.isDebugEnabled()) {
						log.debug("formBean.getProductCode()= " + formBean.getProductCode());
						log.debug("methodPending.getProductCode()= " + methodPending.getProductCode());
					}
					if (!formBean.getProductCode().equals(methodPending.getProductCode())) {
						valid = false;
						if (!pc.containsKey(methodPending.getProductCode())) {
							method = method + methodPending.getProductCode() + ", ";
							pc.put(methodPending.getProductCode(), methodPending.getProductCode());
						}
					}
				}

			}
			pc = null;
			if (!valid) {
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DESELECT_LOV,
						new Object[] { "Charge Method", method.trim().substring(0, method.length() - 1) });
				return null;
			}
		}

		// to check whether sub product exist or not
		// if yes not allow to deselect the lov list
		// validate charge method
		PandLSetupBean productPLAccountBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		if (productPLAccountBean.getSubPLAccountList() != null) {
			valid = true;
			method = "";
			pc = new HashMap<String, String>();
			for (ProductPLAccountPendingModel pandlPending : productPLAccountBean.getSubPLAccountList()) {
				if (pandlPending.getStatus() == null
						|| (pandlPending.getStatus() != null && (pandlPending.getStatus() != Status.DELETE_DRAFT.value()
								&& pandlPending.getStatus() != Status.DELETE_REJECT.value()
								&& pandlPending.getStatus() != Status.DELETE_SUBMIT.value()))) {
					if (log.isDebugEnabled()) {
						log.debug("formBean.getProductCode()= " + formBean.getProductCode());
						log.debug("pandlPending.getProductCode()= " + pandlPending.getProductCode());
					}
					if (!formBean.getProductCode().equals(pandlPending.getProductCode())) {
						valid = false;
						if (!pc.containsKey(pandlPending.getProductCode())) {
							method = method + pandlPending.getProductCode() + ", ";
							pc.put(pandlPending.getProductCode(), pandlPending.getProductCode());
						}
					}
				}

			}
			pc = null;
			if (!valid) {
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DESELECT_LOV,
						new Object[] { "Charge Method", method.trim().substring(0, method.length() - 1) });
				return null;
			}
		}

		// to check the lov remove from pick list against the chargeCode table
		// list whether it is using it or not
		List<ChargeCodePendingModel> chargeList = chargeBean.getChargeCodeList();
		if (chargeList != null) {
			HashMap<String, String> chargeMap = new HashMap<String, String>();
			List<String> strChargeList = new ArrayList<String>();
			for (ChargeCodePendingModel model : chargeList) {
				if (model.getStatus() != Status.DELETE_DRAFT.value()
						&& model.getStatus() != Status.DELETE_REJECT.value()
						&& model.getStatus() != Status.DELETE_SUBMIT.value()) {
					if (model.getLovAttribute() != null) {
						strChargeList.add(model.getLovAttribute().toString());
						chargeMap.put(model.getLovAttribute().toString(), model.getChargeCode());
						if (log.isDebugEnabled()) {
							// log.debug("getSelectedLovAttribute.size() = " +
							// formBean.getSelectedLovAttribute().size());
							log.debug("model.getLovAttribute().toString() = " + model.getLovAttribute().toString());
						}
					}
				}
			}
			List<String> subtractList = compareChargeList(strChargeList, formBean.getSelectedLOVAttributesList());
			if (subtractList.size() > 0) {
				String chargeCode = "";
				String lovAttribute = "";
				for (String lovId : subtractList) {
					if (log.isDebugEnabled()) {
						log.debug("lovId   = " + lovId);
						log.debug("chargeList   = " + chargeList);
						log.debug("chargeBean.getChargeCodeList()   = " + chargeBean.getChargeCodeList());
					}
					/*
					 * for (ChargeCodePendingModel charge : chargeList) {
					 * log.debug("model.getLovAttribute().toString() = " +
					 * charge.getLovAttribute().toString());
					 * if(lovId.equals(charge.getLovAttribute().toString())){
					 * if(StringUtils.hasLength(chargeCode)){ chargeCode = chargeCode +"," +
					 * charge.getChargeCode(); }else{ chargeCode = charge.getChargeCode(); } } }
					 */

					if (chargeMap.containsKey(lovId)) {
						if (StringUtils.hasLength(chargeMap.get(lovId))) {
							chargeCode = chargeCode + "," + chargeMap.get(lovId);
						} else {
							chargeCode = chargeMap.get(lovId);
						}
					}

					String lovDesc = "";
					if (formBean.getLovMap().containsKey(lovId)) {
						lovDesc = formBean.getLovMap().get(lovId);
					}
					if (StringUtils.hasLength(lovAttribute)) {

						lovAttribute = lovAttribute + "," + lovDesc;
					} else {
						lovAttribute = lovDesc;
					}
				}
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGECODELIST,
						new Object[] { lovAttribute, chargeCode });
				// formBean.setSelectedLOVAttributesList(new
				// ArrayList<String>());
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				return null;
			}
		}

		int selectedSize = formBean.getSelectedLOVAttributesList().size();
		if (selectedSize > MAX_LOV_SELECTED) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_NOTGREATERTHAN, new Object[] {
					FacesUtil.getMessageByKey(MessageConstants.PRODUCTDEFINITION_ERROR_MAXLOV), MAX_LOV_SELECTED });
			// LovAttributeBean lovBean =
			// formBean.getSelectedLovAttribute().get(selectedSize-1);
			// formBean.getSelectedLovAttribute().remove(selectedSize-1);
			// formBean.getLovAttribute().add(lovBean);
			formBean.setIsGenerateAllowed(false);
			return null;
		}
		/*
		 * for (LovAttributeBean lovBean : formBean.getSelectedLovAttribute()) {
		 * if(log.isDebugEnabled()){ log.debug("getAttributeId = " +
		 * lovBean.getAttributeId()); log.debug("getAttributeLabel = " +
		 * lovBean.getAttributeLabel()); } }
		 */
		ProductLovModel lovModel = null;
		List<ProductLovModel> lovList = new ArrayList<ProductLovModel>();
		for (String selectedLov : formBean.getSelectedLOVAttributesList()) {
			if (log.isDebugEnabled()) {
				log.debug("selectedLov = " + selectedLov);
			}
			lovModel = new ProductLovModel();
			lovModel.setLovAttributeId(Long.parseLong(selectedLov));
			String lovDesc = null;
			if (formBean.getLovMap().containsKey(selectedLov)) {
				lovDesc = formBean.getLovMap().get(selectedLov);
			}
			/* lovModel.setProductLovDescription( lovDesc ); */
			lovList.add(lovModel);
		}

		/*
		 * if(formBean.getMaintenanceStep().equals(MaintenanceStep.New.value()) &&
		 * (formBean.getDefSelectedLOVAttributesList()==null ||
		 * formBean.getDefSelectedLOVAttributesList().size()==0)){
		 */
		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			if (log.isDebugEnabled()) {
				log.debug("Maintenance step is new.");
			}
			// formBean.setBkSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList())
			// formBean.setDefSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList());
			if (selectedSize > 0) {// &&formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())){
				// List<String> compareList =
				// compareChargeList(formBean.getDefSelectedChargeOnList(),
				// formBean.getSelectedLOVAttributesList());
				// if(list)
				// if(!validateCartesianTableLov(formBean)){
				formBean.setIsGenerateAllowed(true);
				formBean.setGenerateNeeded(true);
				// }
			}
			formBean.setProductLovList(lovList);
		} else {

			// enable the generate cartesian button
			// to add on the checking if only modify allowed
			if (selectedSize > 0) {// &&formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())){
				// List<String> compareList =
				// compareChargeList(formBean.getDefSelectedChargeOnList(),
				// formBean.getSelectedLOVAttributesList());
				// if(list)
				if (!formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
					if (!validateCartesianTableLov(formBean)) {
						formBean.setIsGenerateAllowed(true);
						formBean.setGenerateNeeded(true);
					} else {
						formBean.setGenerateNeeded(false);
					}
				}
			} else {
				formBean.setIsGenerateAllowed(false);
			}
			formBean.setProductLovList(lovList);

			/*
			 * if(!validateCartesianTableLov(formBean)){ //if(selectedSize>0){
			 * formBean.setGenerateNeeded(true); //}else{ //
			 * formBean.setGenerateNeeded(false); //} }else{
			 * formBean.setGenerateNeeded(false); }
			 */
		}
		if (log.isDebugEnabled()) {
			log.debug("formBean.isGenerateAllowed()   = " + formBean.getIsGenerateAllowed());
			log.debug("formBean.isGenerateNeeded()   = " + formBean.isGenerateNeeded());
			// log.debug("validateCartesianTableLov(formBean) = " +
			// validateCartesianTableLov(formBean));
		}
		formBean.setBkSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList());
		return null;
	}

	public String validateLovSelect(SelectEvent event) {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);

		if (log.isDebugEnabled()) {
			// log.debug("getSelectedLovAttribute.size() = " +
			// formBean.getSelectedLovAttribute().size());
			log.debug("getSelectedLovAttribute.size() = " + formBean.getSelectedLOVAttributesList().size());
		}

		// to check whether sub product exist or not
		// if yes not allow to deselect the lov list
		// validate charge method
		ProductChargeBean productChargeBean = (ProductChargeBean) super.getManagedBean(
				ProductChargeBean.BACKING_BEAN_NAME);
		boolean valid = true;
		String method = "";
		HashMap<String, String> pc = new HashMap<String, String>();
		if (productChargeBean.getChargeCodeList() != null) {
			for (ChargeCodePendingModel chargePending : productChargeBean.getChargeCodeList()) {
				if (chargePending.getStatus() != Status.DELETE_DRAFT.value()
						&& chargePending.getStatus() != Status.DELETE_REJECT.value()
						&& chargePending.getStatus() != Status.DELETE_SUBMIT.value()) {
					if (log.isDebugEnabled()) {
						log.debug("formBean.getProductCode()= " + formBean.getProductCode());
						log.debug("methodPending.getProductCode()= " + chargePending.getProductCode());
					}
					if (!formBean.getProductCode().equals(chargePending.getProductCode())) {
						valid = false;
						if (!pc.containsKey(chargePending.getProductCode())) {
							method = method + chargePending.getProductCode() + ", ";
							pc.put(chargePending.getProductCode(), chargePending.getProductCode());
						}
					}
				}
			}
			pc = null;
			if (!valid) {
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DESELECT_LOV,
						new Object[] { "Charge Code", method.trim().substring(0, method.length() - 1) });
				return null;
			}
		}

		// to check whether sub product exist or not
		// if yes not allow to deselect the lov list
		// validate charge method
		MethodBean productMethodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		if (productMethodBean.getSubMethodList() != null) {
			valid = true;
			method = "";
			pc = new HashMap<String, String>();
			for (ChargeMethodPendingModel methodPending : productMethodBean.getSubMethodList()) {
				if (methodPending.getStatus() == null || (methodPending.getStatus() != null
						&& (methodPending.getStatus() != Status.DELETE_DRAFT.value()
								&& methodPending.getStatus() != Status.DELETE_REJECT.value()
								&& methodPending.getStatus() != Status.DELETE_SUBMIT.value()))) {
					if (log.isDebugEnabled()) {
						log.debug("formBean.getProductCode()= " + formBean.getProductCode());
						log.debug("methodPending.getProductCode()= " + methodPending.getProductCode());
					}
					if (!formBean.getProductCode().equals(methodPending.getProductCode())) {
						valid = false;
						if (!pc.containsKey(methodPending.getProductCode())) {
							method = method + methodPending.getProductCode() + ", ";
							pc.put(methodPending.getProductCode(), methodPending.getProductCode());
						}
					}
				}

			}
			pc = null;
			if (!valid) {
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DESELECT_LOV,
						new Object[] { "Charge Method", method.trim().substring(0, method.length() - 1) });
				return null;
			}
		}

		// to check whether sub product exist or not
		// if yes not allow to deselect the lov list
		// validate charge method
		PandLSetupBean productPLAccountBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		if (productPLAccountBean.getSubPLAccountList() != null) {
			valid = true;
			method = "";
			pc = new HashMap<String, String>();
			for (ProductPLAccountPendingModel pandlPending : productPLAccountBean.getSubPLAccountList()) {
				if (pandlPending.getStatus() == null
						|| (pandlPending.getStatus() != null && (pandlPending.getStatus() != Status.DELETE_DRAFT.value()
								&& pandlPending.getStatus() != Status.DELETE_REJECT.value()
								&& pandlPending.getStatus() != Status.DELETE_SUBMIT.value()))) {
					if (log.isDebugEnabled()) {
						log.debug("formBean.getProductCode()= " + formBean.getProductCode());
						log.debug("pandlPending.getProductCode()= " + pandlPending.getProductCode());
					}
					if (!formBean.getProductCode().equals(pandlPending.getProductCode())) {
						valid = false;
						if (!pc.containsKey(pandlPending.getProductCode())) {
							method = method + pandlPending.getProductCode() + ", ";
							pc.put(pandlPending.getProductCode(), pandlPending.getProductCode());
						}
					}
				}

			}
			pc = null;
			if (!valid) {
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DESELECT_LOV,
						new Object[] { "Charge Method", method.trim().substring(0, method.length() - 1) });
				return null;
			}
		}

		// to check the lov remove from pick list against the chargeCode table
		// list whether it is using it or not
		List<ChargeCodePendingModel> chargeList = chargeBean.getChargeCodeList();
		if (chargeList != null) {
			HashMap<String, String> chargeMap = new HashMap<String, String>();
			List<String> strChargeList = new ArrayList<String>();
			for (ChargeCodePendingModel model : chargeList) {
				if (model.getStatus() != Status.DELETE_DRAFT.value()
						&& model.getStatus() != Status.DELETE_REJECT.value()
						&& model.getStatus() != Status.DELETE_SUBMIT.value()) {
					if (model.getLovAttribute() != null) {
						strChargeList.add(model.getLovAttribute().toString());
						chargeMap.put(model.getLovAttribute().toString(), model.getChargeCode());
						if (log.isDebugEnabled()) {
							// log.debug("getSelectedLovAttribute.size() = " +
							// formBean.getSelectedLovAttribute().size());
							log.debug("model.getLovAttribute().toString() = " + model.getLovAttribute().toString());
						}
					}
				}
			}
			List<String> subtractList = compareChargeList(strChargeList, formBean.getSelectedLOVAttributesList());
			if (subtractList.size() > 0) {
				String chargeCode = "";
				String lovAttribute = "";
				for (String lovId : subtractList) {
					if (log.isDebugEnabled()) {
						log.debug("lovId   = " + lovId);
						log.debug("chargeList   = " + chargeList);
						log.debug("chargeBean.getChargeCodeList()   = " + chargeBean.getChargeCodeList());
					}
					/*
					 * for (ChargeCodePendingModel charge : chargeList) {
					 * log.debug("model.getLovAttribute().toString() = " +
					 * charge.getLovAttribute().toString());
					 * if(lovId.equals(charge.getLovAttribute().toString())){
					 * if(StringUtils.hasLength(chargeCode)){ chargeCode = chargeCode +"," +
					 * charge.getChargeCode(); }else{ chargeCode = charge.getChargeCode(); } } }
					 */

					if (chargeMap.containsKey(lovId)) {
						if (StringUtils.hasLength(chargeMap.get(lovId))) {
							chargeCode = chargeCode + "," + chargeMap.get(lovId);
						} else {
							chargeCode = chargeMap.get(lovId);
						}
					}

					String lovDesc = "";
					if (formBean.getLovMap().containsKey(lovId)) {
						lovDesc = formBean.getLovMap().get(lovId);
					}
					if (StringUtils.hasLength(lovAttribute)) {

						lovAttribute = lovAttribute + "," + lovDesc;
					} else {
						lovAttribute = lovDesc;
					}
				}
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGECODELIST,
						new Object[] { lovAttribute, chargeCode });
				// formBean.setSelectedLOVAttributesList(new
				// ArrayList<String>());
				formBean.setSelectedLOVAttributesList(formBean.getBkSelectedLOVAttributesList());
				return null;
			}
		}

		int selectedSize = formBean.getSelectedLOVAttributesList().size();
		if (selectedSize > MAX_LOV_SELECTED) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_NOTGREATERTHAN, new Object[] {
					FacesUtil.getMessageByKey(MessageConstants.PRODUCTDEFINITION_ERROR_MAXLOV), MAX_LOV_SELECTED });
			// LovAttributeBean lovBean =
			// formBean.getSelectedLovAttribute().get(selectedSize-1);
			// formBean.getSelectedLovAttribute().remove(selectedSize-1);
			// formBean.getLovAttribute().add(lovBean);
			formBean.setIsGenerateAllowed(false);
			return null;
		}
		/*
		 * for (LovAttributeBean lovBean : formBean.getSelectedLovAttribute()) {
		 * if(log.isDebugEnabled()){ log.debug("getAttributeId = " +
		 * lovBean.getAttributeId()); log.debug("getAttributeLabel = " +
		 * lovBean.getAttributeLabel()); } }
		 */
		ProductLovModel lovModel = null;
		List<ProductLovModel> lovList = new ArrayList<ProductLovModel>();
		for (String selectedLov : formBean.getSelectedLOVAttributesList()) {
			if (log.isDebugEnabled()) {
				log.debug("selectedLov = " + selectedLov);
			}
			lovModel = new ProductLovModel();
			lovModel.setLovAttributeId(Long.parseLong(selectedLov));
			String lovDesc = null;
			if (formBean.getLovMap().containsKey(selectedLov)) {
				lovDesc = formBean.getLovMap().get(selectedLov);
			}
			/* lovModel.setProductLovDescription( lovDesc ); */
			lovList.add(lovModel);
		}

		/*
		 * if(formBean.getMaintenanceStep().equals(MaintenanceStep.New.value()) &&
		 * (formBean.getDefSelectedLOVAttributesList()==null ||
		 * formBean.getDefSelectedLOVAttributesList().size()==0)){
		 */
		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			if (log.isDebugEnabled()) {
				log.debug("Maintenance step is new.");
			}
			// formBean.setBkSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList())
			// formBean.setDefSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList());
			if (selectedSize > 0) {// &&formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())){
				// List<String> compareList =
				// compareChargeList(formBean.getDefSelectedChargeOnList(),
				// formBean.getSelectedLOVAttributesList());
				// if(list)
				// if(!validateCartesianTableLov(formBean)){
				formBean.setIsGenerateAllowed(true);
				formBean.setGenerateNeeded(true);
				// }
			}
			formBean.setProductLovList(lovList);
		} else {

			// enable the generate cartesian button
			// to add on the checking if only modify allowed
			if (selectedSize > 0) {// &&formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())){
				// List<String> compareList =
				// compareChargeList(formBean.getDefSelectedChargeOnList(),
				// formBean.getSelectedLOVAttributesList());
				// if(list)
				if (!formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
					if (!validateCartesianTableLov(formBean)) {
						formBean.setIsGenerateAllowed(true);
						formBean.setGenerateNeeded(true);
					} else {
						formBean.setGenerateNeeded(false);
					}
				}
			} else {
				formBean.setIsGenerateAllowed(false);
			}
			formBean.setProductLovList(lovList);

			/*
			 * if(!validateCartesianTableLov(formBean)){ //if(selectedSize>0){
			 * formBean.setGenerateNeeded(true); //}else{ //
			 * formBean.setGenerateNeeded(false); //} }else{
			 * formBean.setGenerateNeeded(false); }
			 */
		}
		if (log.isDebugEnabled()) {
			log.debug("formBean.isGenerateAllowed()   = " + formBean.getIsGenerateAllowed());
			log.debug("formBean.isGenerateNeeded()   = " + formBean.isGenerateNeeded());
			// log.debug("validateCartesianTableLov(formBean) = " +
			// validateCartesianTableLov(formBean));
		}
		formBean.setBkSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList());
		return null;
	}

	private boolean validateSelectedChargeOn() {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);

		// US-GER Product Group Sanjeevi Start
		List<String> selectedList = new ArrayList<String>();
		List<AvailableCharrgeOnBean> beans = formBean.getSelectedChargeOnList();
		;
		for (AvailableCharrgeOnBean bean : beans) {
			selectedList.add(bean.getAttributeId().toString());

		}
		// US-GER Product Group Sanjeevi End
		if (selectedList != null && !selectedList.isEmpty()) {
			MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
			List<ChargeMethodPendingModel> chargeMethodList = methodBean.getSubMethodList();
			if (chargeMethodList != null && !chargeMethodList.isEmpty()) {
				for (ChargeMethodPendingModel chargeMethod : chargeMethodList) {
					if (chargeMethod.getChargeOn1() != null
							&& !selectedList.contains(chargeMethod.getChargeOn1().toString())) {
						FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGEONNOTFOUND,
								new Object[] {
										FacesUtil.getMessageByKey(
												MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON1),
										chargeMethod.getChargeOn1Desc(), FacesUtil.getMessageByKey(
												MessageConstants.PRODUCTDEFINITION_SELECTEDCHARGEONATTRIBS) });

					}
					if (!userContext.getSelectedBusinessSegment().equals(BusinessSegment.BANCA.value())) {
						if (chargeMethod.getChargeOn2() != null
								&& !selectedList.contains(chargeMethod.getChargeOn2().toString())) {
							FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGEONNOTFOUND,
									new Object[] {
											FacesUtil.getMessageByKey(
													MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON2),
											chargeMethod.getChargeOn2Desc(), FacesUtil.getMessageByKey(
													MessageConstants.PRODUCTDEFINITION_SELECTEDCHARGEONATTRIBS) });

						}
					}
					// GPBS-394 [S] - the below combinations are not allowed
					if ((PricingConstants.ChargeMethod.FLAT_RATE.value().equals(chargeMethod.getComputationMethod())
							|| PricingConstants.ChargeMethod.FLAT_RATE_HYBRID.value()
									.equals(chargeMethod.getComputationMethod())
							|| PricingConstants.ChargeMethod.TIER_RATE.value()
									.equals(chargeMethod.getComputationMethod())
							|| PricingConstants.ChargeMethod.TIER_RATE_HYBRID.value()
									.equals(chargeMethod.getComputationMethod()))
							&& chargeMethod.getChargeOn1() != null && chargeMethod.getChargeOn1() > 0
							&& BaseConstants.ChargeOnType.COUNT.value()
									.equals(ChargeParamUtil.getChargeOnType(chargeMethod.getChargeOn1(),
											chargeMethod.getCountryCode(),
											MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON1))) {
						FacesUtil.addErrorMessage(null, MessageConstants.CHARGEPARAM_CHARGEMETHOD_CHARGEON_ERROR,
								new Object[] { chargeMethod.getComputationMethod(), chargeMethod.getChargeOn1Desc() });
					} else if ((PricingConstants.ChargeMethod.MULTI_FLAT_RATE.value()
							.equals(chargeMethod.getComputationMethod())
							|| PricingConstants.ChargeMethod.MULTI_TIER_RATE.value()
									.equals(chargeMethod.getComputationMethod()))
							&& chargeMethod.getChargeOn1() != null && chargeMethod.getChargeOn1() > 0
							&& BaseConstants.ChargeOnType.COUNT.value()
									.equals(ChargeParamUtil.getChargeOnType(chargeMethod.getChargeOn1(),
											chargeMethod.getCountryCode(),
											MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON1))
							&& chargeMethod.getChargeOn2() != null && chargeMethod.getChargeOn2() > 0
							&& BaseConstants.ChargeOnType.COUNT.value()
									.equals(ChargeParamUtil.getChargeOnType(chargeMethod.getChargeOn2(),
											chargeMethod.getCountryCode(),
											MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON1))) {
						FacesUtil.addErrorMessage(null, MessageConstants.CHARGEPARAM_CHARGEMETHOD_CHARGEON_ERROR,
								new Object[] { chargeMethod.getComputationMethod(),
										chargeMethod.getChargeOn1Desc() + " and " + chargeMethod.getChargeOn2Desc() });
					} else if ((PricingConstants.ChargeMethod.MULTI_FLAT_RATE.value()
							.equals(chargeMethod.getComputationMethod())
							|| PricingConstants.ChargeMethod.MULTI_TIER_RATE.value()
									.equals(chargeMethod.getComputationMethod()))
							&& chargeMethod.getChargeOn1() != null && chargeMethod.getChargeOn1() > 0
							&& BaseConstants.ChargeOnType.AMOUNT.value()
									.equals(ChargeParamUtil.getChargeOnType(chargeMethod.getChargeOn1(),
											chargeMethod.getCountryCode(),
											MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON1))
							&& chargeMethod.getChargeOn2() != null && chargeMethod.getChargeOn2() > 0
							&& BaseConstants.ChargeOnType.COUNT.value()
									.equals(ChargeParamUtil.getChargeOnType(chargeMethod.getChargeOn2(),
											chargeMethod.getCountryCode(),
											MessageConstants.CHARGEPARAM_CHARGEMETHOD_LABEL_CHARGEON1))) {
						FacesUtil.addErrorMessage(null, MessageConstants.CHARGEPARAM_CHARGEMETHOD_CHARGEON_ERROR,
								new Object[] { chargeMethod.getComputationMethod(),
										chargeMethod.getChargeOn1Desc() + " and " + chargeMethod.getChargeOn2Desc() });
					}
					// GPBS-394 [E]
				}
				if (FacesUtil.getMessageCount() > 0) {
					return false;
				}
			}
		}
		return true;
	}

	// Added by Ramadevi for product mapping validation changes in dec release .
	private boolean validateProductCharge() {
		ProductChargeBean formBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);

		// Tax and non tax charge code validation against global product

		Boolean errFlag = Boolean.TRUE;

		StaticDataSetupModel model = this.productChargeService.getCountryTaxSetup(formBean.getCountryCode());
		if (model != null) {
			if (model.getColumn1() != null && model.getColumn1().equalsIgnoreCase("Yes") && model.getColumn2() != null
					&& model.getColumn2().equalsIgnoreCase("Yes")) {

				if (formBean.getGlobalGpbsProductList() != null && formBean.getChargeCodeList() != null) {
					Boolean taxFlag = Boolean.FALSE;
					Boolean nonTaxFlag = Boolean.FALSE;
					for (GlobalGpbsProductPendingModel globalprod : formBean.getGlobalGpbsProductList()) {
						if (globalprod.getGlobalProductId() != null) {
							for (GlobalGpbsProductPendingModel innerProd : formBean.getGlobalGpbsProductList()) {
								if (innerProd.getGlobalProductId() != null) {
									if (globalprod.getChargeCode() != null
											&& globalprod.getProductMapYN().equalsIgnoreCase(BaseConstants.YES_FLAG)) {
										if (globalprod.getChargeCode()
												.contains(PricingConstants.TaxOrNoTaxChargeCode.NOVAT.value())
												|| globalprod.getChargeCode()
														.contains(PricingConstants.TaxOrNoTaxChargeCode.novat.value())
												|| globalprod.getChargeCode()
														.contains(PricingConstants.TaxOrNoTaxChargeCode.notax.value())
												|| globalprod.getChargeCode().contains(
														PricingConstants.TaxOrNoTaxChargeCode.NOTAX.value())) {
											String parentRemovalChargeCode = globalprod.getChargeCode().substring(0,
													globalprod.getChargeCode().length() - 5);
											for (ChargeCodePendingModel pend : formBean.getChargeCodeList()) {
												if (pend.getTaxTypeCode() != null
														&& StringUtils.hasLength(pend.getTaxTypeCode())
														&& innerProd.getChargeCode()
																.equalsIgnoreCase(pend.getChargeCode())) {
													taxFlag = Boolean.TRUE;
												} else if (pend.getTaxTypeCode() == null) {
													nonTaxFlag = Boolean.TRUE;
												}
											}
											if (parentRemovalChargeCode.equalsIgnoreCase(innerProd.getChargeCode())
													&& !(globalprod.getGlobalProductId()
															.equals(innerProd.getGlobalProductId()))
													&& taxFlag) {
												errFlag = Boolean.FALSE;
											}
										} else {
											for (ChargeCodePendingModel pend : formBean.getChargeCodeList()) {
												if (pend.getTaxTypeCode() != null
														&& StringUtils.hasLength(pend.getTaxTypeCode())
														&& innerProd.getChargeCode()
																.equalsIgnoreCase(pend.getChargeCode())) {
													taxFlag = Boolean.TRUE;
												} else if ((pend.getTaxTypeCode() == null
														|| pend.getTaxTypeCode() == "")
														&& innerProd.getChargeCode()
																.equalsIgnoreCase(pend.getChargeCode())) {
													nonTaxFlag = Boolean.TRUE;
												}
											}
											String checkForNoVat = globalprod.getChargeCode().concat("NOVAT");
											if (checkForNoVat.equalsIgnoreCase(innerProd.getChargeCode())
													&& !(globalprod.getGlobalProductId()
															.equals(innerProd.getGlobalProductId()))
													&& nonTaxFlag) {
												errFlag = Boolean.FALSE;
											} else {
												String checkForNotax = globalprod.getChargeCode().concat("NOTAX");
												if (checkForNotax.equalsIgnoreCase(innerProd.getChargeCode())
														&& !(globalprod.getGlobalProductId()
																.equals(innerProd.getGlobalProductId()))
														&& nonTaxFlag) {
													errFlag = Boolean.FALSE;
												}
											}
										}

									}
								}
							}
						}
					}
				}

			}

			if (!errFlag) {
				FacesUtil.addErrorMessage(null, MessageConstants.ERROR_TAX_NOTAX_CHARGECODE_MAP_MSG,
						new Object[] { formBean.getGlobalGpbsProductList().get(0).getProductCode() });
			}
		}

		/*
		 * if (formBean.getGlobalGpbsProductList() != null &&
		 * formBean.getChargeCodeList()!=null) { Boolean taxFlag = Boolean.FALSE;
		 * Boolean nonTaxFlag = Boolean.FALSE; Boolean errFlag =Boolean.TRUE;
		 * 
		 * 
		 * for(ChargeCodePendingModel pend :formBean.getChargeCodeList()){
		 * if(pend.getTaxTypeCode()!=null &&
		 * StringUtils.hasLength(pend.getTaxTypeCode())){ taxFlag = Boolean.TRUE; } else
		 * if(pend.getTaxTypeCode()==null ){ nonTaxFlag = Boolean.TRUE; } } if(taxFlag
		 * && !nonTaxFlag){//For modifying
		 * 
		 * for(GlobalGpbsProductPendingModel gloPend:
		 * formBean.getGlobalGpbsProductList()){ if(formBean.getTaxTypeCode() ==null &&
		 * formBean.getChargeGlobalProductId()!=null &&
		 * !formBean.getChargeGlobalProductId().equals(gloPend.getGlobalProductId())){
		 * errFlag=Boolean.FALSE;
		 * 
		 * } } } else if(!taxFlag && nonTaxFlag){ if(formBean.getChargeCodeList().size()
		 * >1){ for(GlobalGpbsProductPendingModel gloPend:
		 * formBean.getGlobalGpbsProductList()){ if(formBean.getTaxTypeCode()!=null &&
		 * formBean.getChargeGlobalProductId()!=null &&
		 * !formBean.getChargeGlobalProductId().equals(gloPend.getGlobalProductId())){
		 * errFlag=Boolean.FALSE; } } }
		 * 
		 * } else if(taxFlag && nonTaxFlag){ for(GlobalGpbsProductPendingModel gloPend:
		 * formBean.getGlobalGpbsProductList()){ if((formBean.getTaxTypeCode()!=null ||
		 * formBean.getTaxTypeCode()==null) && formBean.getChargeGlobalProductId()!=null
		 * &&
		 * !formBean.getChargeGlobalProductId().equals(gloPend.getGlobalProductId())){
		 * errFlag=Boolean.FALSE;
		 * 
		 * } } }
		 * 
		 * if(!errFlag){ FacesUtil.addErrorMessage(null,
		 * MessageConstants.ERROR_TAX_NOTAX_CHARGECODE_MAP_MSG, new Object[] {
		 * formBean.getProductCode() }); } }
		 */
		return true;

	}

	public boolean validateProductChrgeMethodAndChargeOn() {

		ProductChargeBean formBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		Boolean errFlag = Boolean.TRUE;

		if (formBean.getGlobalGpbsProductList() != null) {
			List<String> computatioMethodList = new ArrayList<String>();
			List<String> chargeOn1List = new ArrayList<String>();
			for (GlobalGpbsProductPendingModel globalprod : formBean.getGlobalGpbsProductList()) {// globalprod.getActionType().equals("CHANGE")
				MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
				if (globalprod.getProductMapYN().equalsIgnoreCase(BaseConstants.YES_FLAG)
						&& (globalprod.getActionType() != null && (globalprod.getActionType().equalsIgnoreCase("NEW")
								|| (globalprod.getActionType().equalsIgnoreCase("CHANGE"))))) {
					List<ChargeMethodPendingModel> beanChargeMethodList = methodBean.getSubMethodList();
					if (globalprod.getGlobalProductId() != null) {
						List<GlobalProductChargeCheckVO> chargeMethodForTemplateListDB = productChargeService
								.getChargeOnComputationMethod(formBean.getCountryCode(), globalprod.getProductCode(),
										globalprod.getChargeCode(), globalprod.getGlobalProductId());

						if (chargeMethodForTemplateListDB != null && !chargeMethodForTemplateListDB.isEmpty()) {

							// add elements to al, including duplicates

							Set<GlobalProductChargeCheckVO> hs = new HashSet<GlobalProductChargeCheckVO>();
							hs.addAll(chargeMethodForTemplateListDB);
							chargeMethodForTemplateListDB.clear();
							chargeMethodForTemplateListDB.addAll(hs);

							for (GlobalProductChargeCheckVO chargeMethodTemplateDB : chargeMethodForTemplateListDB) {
								String available = null;// check
								if (beanChargeMethodList != null && !beanChargeMethodList.isEmpty()) {
									for (ChargeMethodPendingModel beanChargeMethod : beanChargeMethodList) {
										if (beanChargeMethod.getProductCode()
												.equalsIgnoreCase(globalprod.getProductCode())
												&& beanChargeMethod.getChargeCode()
														.equalsIgnoreCase(globalprod.getChargeCode())) {

											available = "false";
											String chargeOnType1 = null;
											String chargeOnType2 = null;
											if (beanChargeMethod.getChargeOn1() != null) {
												// get the charge_on_type1 for bean Chargeon details

												chargeOnType1 = ChargeParamUtil.getChargeOnType(
														beanChargeMethod.getChargeOn1(),
														beanChargeMethod.getCountryCode(), null);
											}
											if (beanChargeMethod.getChargeOn2() != null) {
												// get the charge_on_type2 for bean Chargeon details
												chargeOnType2 = ChargeParamUtil.getChargeOnType(
														beanChargeMethod.getChargeOn2(),
														beanChargeMethod.getCountryCode(), null);
											}

											if ((chargeMethodTemplateDB.getComputationMethod()
													.equalsIgnoreCase(beanChargeMethod.getComputationMethod())
													&& (chargeOnType1 != null && chargeMethodTemplateDB
															.getChargeOnType1().equalsIgnoreCase(chargeOnType1)))) {
												if (chargeMethodTemplateDB.getChargeOnType2() == null) {
													available = "true";
													break;
												} else if (chargeOnType2 != null && chargeMethodTemplateDB
														.getChargeOnType2().equals(chargeOnType2)) {
													available = "true";
													break;
												}
											}
										} else {
											List<String> chargeCodeList = new ArrayList<String>();
											for (ChargeMethodPendingModel beanChargeCode : beanChargeMethodList) {
												chargeCodeList.add(beanChargeCode.getChargeCode());
											}
											if (!chargeCodeList.contains(globalprod.getChargeCode())) {
												available = "false";
											}

										}

									}
								}
								if (available != null && available.equalsIgnoreCase("false")) {
									if (chargeMethodTemplateDB.getChargeOnType2() == null) {
										computatioMethodList.add(globalprod.getChargeCode() + " | "
												+ chargeMethodTemplateDB.getComputationMethod() + " | "
												+ chargeMethodTemplateDB.getChargeOnType1());
									} else {
										computatioMethodList.add(globalprod.getChargeCode() + " | "
												+ chargeMethodTemplateDB.getComputationMethod() + " | "
												+ chargeMethodTemplateDB.getChargeOnType1() + " | "
												+ chargeMethodTemplateDB.getChargeOnType2());
									}
								}
							}
						}
					}
				}
			}
			StringBuffer errorSB = new StringBuffer();
			int index = 0;
			String error = "";
			if (!computatioMethodList.isEmpty()) {
				for (String chargeMethodChargeOn : computatioMethodList) {
					errorSB.append(chargeMethodChargeOn);
					if (index < computatioMethodList.size() - 1) {
						errorSB.append(", ");
					}
					index++;
				}
				error = errorSB.toString();
				if (error != null && !error.equals("")) {
					FacesUtil.addErrorMessage(null, MessageConstants.ERROR_MANADATORY_CHARGE_METHOD_CHARGE_ON_TEMPLATE,
							new Object[] { error });
					errFlag = Boolean.FALSE;
				}
			}
		}
		return errFlag;
	}

	private boolean validateCartesianTableLov(ProductDefinitionBean formBean) {
		// if(!formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())){
		if (!formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			List<String> defaultLov = formBean.getDefSelectedLOVAttributesList();
			List<String> selectedLov = formBean.getSelectedLOVAttributesList();
			int defaultLovSize = defaultLov.size();
			int selectedLovSize = selectedLov.size();
			if (log.isDebugEnabled()) {
				log.debug("selectedLovSize  = " + selectedLovSize);
				log.debug("defaultLovSize  = " + defaultLovSize);
			}
			if (defaultLovSize != selectedLovSize) {
				return false;
			} else {
				boolean valid = true;
				for (int i = 0; i < defaultLov.size(); i++) {
					if (log.isDebugEnabled()) {
						log.debug("defaultLov.get(i)  = " + defaultLov.get(i));
						log.debug("selectedLov.get(i)  = " + selectedLov.get(i));
						log.debug("defaultLov.get(i).equals(selectedLov.get(i)) = "
								+ defaultLov.get(i).equals(selectedLov.get(i)));
					}
					if (!defaultLov.get(i).equals(selectedLov.get(i))) {
						valid = false;
						break;
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("valid  = " + valid);
				}
				if (!valid) {
					return false;
				}
			}
			/*
			 * }else{ if(log.isDebugEnabled()){ log.debug("=== else ==== "); } }
			 */
		}
		return true;
	}

	private boolean validateCartesianTableDesc(ProductDefinitionBean formBean) {

		SubProductVO[] newLovProductArray = formBean.getLovProductsArray();
		// SubProductVO[] defLovProductArray =
		// formBean.getDefaultLovProductsArray();
		if (newLovProductArray != null) {
			SubProductVO[] tempArray = new SubProductVO[newLovProductArray.length];
			for (int i = 0; i < newLovProductArray.length; i++) {
				SubProductVO prodVo = newLovProductArray[i];
				if (!StringUtils.hasLength(prodVo.getSubProductDesc())) {
					FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED, new Object[] {
							FacesUtil.getMessageByKey(MessageConstants.PRODUCTDEFINITION_SUBPRODUCT_DESCRIPTION) });
					return false;
				}
				if (!formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
					/*
					 * if(log.isDebugEnabled()){
					 * log.debug("defLovProductArray[i].getSubProductDesc() = " +
					 * defLovProductArray[i].getSubProductDesc());
					 * log.debug("newLovProductArray[i].getSubProductDesc() = " +
					 * newLovProductArray[i].getSubProductDesc());
					 * log.debug("defaultLov.get(i).equals(selectedLov.get(i)) = "
					 * +newLovProductArray[i].getSubProductDesc().equals(
					 * defLovProductArray[i].getSubProductDesc())); }
					 */
					if (log.isDebugEnabled()) {
						log.debug(
								"formBean.getDefaultLovProductList() = " + formBean.getDefaultLovProductList().size());
						log.debug("newLovProductArray[i].getSubProductCode() = "
								+ newLovProductArray[i].getSubProductCode());
					}
					for (ProductPendingModel productPending : formBean.getDefaultLovProductList()) {
						if (newLovProductArray[i].getSubProductCode().equals(productPending.getProductCode())) {
							if (log.isDebugEnabled()) {
								log.debug("newLovProductArray[i].getSubProductCode() = "
										+ newLovProductArray[i].getSubProductCode());
								log.debug("productPending.getProductCode()  = " + productPending.getProductCode());
								log.debug("newLovProductArray[i].getSubProductDesc() = "
										+ newLovProductArray[i].getSubProductDesc());
								log.debug("productPending.getDescription() = " + productPending.getDescription());
								log.debug(
										"newLovProductArray[i].getSubProductDesc().equals(productPending.getDescription()) = "
												+ newLovProductArray[i].getSubProductDesc()
														.equals(productPending.getDescription()));
							}
							if (!newLovProductArray[i].getSubProductDesc().equals(productPending.getDescription())) {
								prodVo.setModify(true);
							} else {
								prodVo.setModify(false);
							}
						}
						tempArray[i] = prodVo;
					}
					formBean.setLovProductsArray(null);
					formBean.setLovProductsArray(tempArray);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("tempArray.length  = " + tempArray.length);
			}
		}

		// }
		return true;
	}

	public String renderChargeOn(ActionEvent event) throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		List<ChargeMethodPendingModel> methodList = methodBean.getSubMethodList();
		HashMap<String, ChargeMethodPendingModel> chargeMap = new HashMap<String, ChargeMethodPendingModel>();
//US-GER Product Group Sanjeevi Start
		/*
		 * if (formBean.getSelectedChargeOnList().size() > 0) {
		 * formBean.setIsShowChargeOnAsVolumeFlagDisabled(true); } else {
		 * formBean.setIsShowChargeOnAsVolumeFlagDisabled(false);
		 * formBean.setShowChargeOnAsVolumeFlag(false);
		 * formBean.setVolumeChargeOnAttribute(null); }
		 */

		List<String> avbList = null;
		if (formBean.getSelectedChargeOnList().size() > 0) {
			avbList = new ArrayList<String>();
			List<AvailableCharrgeOnBean> bean = formBean.getSelectedChargeOnList();
			for (AvailableCharrgeOnBean beanTmp : bean) {
				avbList.add(beanTmp.getAttributeId().toString());
			}

			// US-GER Product Group Sanjeevi End
			formBean.setIsShowChargeOnAsVolumeFlagDisabled(true);
		} else {
			formBean.setIsShowChargeOnAsVolumeFlagDisabled(false);
			formBean.setShowChargeOnAsVolumeFlag(false);
			formBean.setVolumeChargeOnAttribute(null);
		}

		if (methodList != null) {
			List<String> strChargeList = new ArrayList<String>();
			for (ChargeMethodPendingModel model : methodList) {
				if (model.getStatus() == null
						|| (model.getStatus() != null && (model.getStatus() != Status.DELETE_DRAFT.value()
								&& model.getStatus() != Status.DELETE_REJECT.value()
								&& model.getStatus() != Status.DELETE_SUBMIT.value()))) {
					if (model.getChargeOn1() != null) {
						if (!chargeMap.containsKey(model.getChargeOn1().toString())) {
							strChargeList.add(model.getChargeOn1().toString());
							chargeMap.put(model.getChargeOn1().toString(), model);
						}
					}
					if (model.getChargeOn2() != null) {
						if (!chargeMap.containsKey(model.getChargeOn2().toString())) {
							strChargeList.add(model.getChargeOn2().toString());
							chargeMap.put(model.getChargeOn2().toString(), model);
						}
					}
					if (log.isDebugEnabled()) {
						// log.debug("getSelectedLovAttribute.size() = " +
						// formBean.getSelectedLovAttribute().size());
						log.debug("model.getChargeOn1().toString() = "
								+ (model.getChargeOn1() == null ? "" : model.getChargeOn1().toString()));
					}
				}

			}
			// chargeMap.clear();
			// US-GER Product Group Sanjeevi start
			List<String> subtractList = compareChargeList(strChargeList, avbList);
			// US-GER Product Group Sanjeevi end
			if (subtractList.size() > 0) {
				String chargeMethod = "";
				String chargeAttribute = "";
				for (String chargeId : subtractList) {
					ChargeMethodPendingModel model = chargeMap.get(chargeId);
					if (StringUtils.hasLength(chargeAttribute)) {
						chargeMethod = chargeMethod + "," + model.getChargeCode();
					} else {
						chargeMethod = model.getChargeCode();
					}

					String chargeDesc = "";
					if (formBean.getChargeOnMap().containsKey(chargeId)) {
						chargeDesc = formBean.getChargeOnMap().get(chargeId);
					}
					if (StringUtils.hasLength(chargeAttribute)) {
						chargeAttribute = chargeAttribute + "," + chargeDesc;
					} else {
						chargeAttribute = chargeDesc;
					}
				}
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGEMETHODLIST,
						new Object[] { chargeAttribute, chargeMethod });
				formBean.setSelectedChargeOnList(formBean.getBkSelectedChargeOnList());
				return null;
			}
		}
		/*
		 * for (String selectedCharge : formBean.getSelectedChargeOnList()) {
		 * if(log.isDebugEnabled()){ log.debug("selectedCharge = " + selectedCharge); }
		 * }
		 */
		if (log.isDebugEnabled()) {
			log.debug("getSelectedChargeOnList.size() = " + formBean.getSelectedChargeOnList().size());
			log.debug("formBean.isGenerateAllowed()   = " + formBean.getIsGenerateAllowed());
		}
		formBean.setBkSelectedChargeOnList(formBean.getSelectedChargeOnList());
		return null;
	}

	public String renderChargeOn(TransferEvent event) throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		List<ChargeMethodPendingModel> methodList = methodBean.getSubMethodList();
		HashMap<String, ChargeMethodPendingModel> chargeMap = new HashMap<String, ChargeMethodPendingModel>();
//US-GER Product Group Sanjeevi Start
		/*
		 * if (formBean.getSelectedChargeOnList().size() > 0) {
		 * formBean.setIsShowChargeOnAsVolumeFlagDisabled(true); } else {
		 * formBean.setIsShowChargeOnAsVolumeFlagDisabled(false);
		 * formBean.setShowChargeOnAsVolumeFlag(false);
		 * formBean.setVolumeChargeOnAttribute(null); }
		 */

		List<String> avbList = null;
		if (formBean.getSelectedChargeOnList().size() > 0) {
			avbList = new ArrayList<String>();
			List<AvailableCharrgeOnBean> bean = formBean.getSelectedChargeOnList();
			for (AvailableCharrgeOnBean beanTmp : bean) {
				avbList.add(beanTmp.getAttributeId().toString());
			}

			// US-GER Product Group Sanjeevi End
			formBean.setIsShowChargeOnAsVolumeFlagDisabled(true);
		} else {
			formBean.setIsShowChargeOnAsVolumeFlagDisabled(false);
			formBean.setShowChargeOnAsVolumeFlag(false);
			formBean.setVolumeChargeOnAttribute(null);
		}

		if (methodList != null) {
			List<String> strChargeList = new ArrayList<String>();
			for (ChargeMethodPendingModel model : methodList) {
				if (model.getStatus() == null
						|| (model.getStatus() != null && (model.getStatus() != Status.DELETE_DRAFT.value()
								&& model.getStatus() != Status.DELETE_REJECT.value()
								&& model.getStatus() != Status.DELETE_SUBMIT.value()))) {
					if (model.getChargeOn1() != null) {
						if (!chargeMap.containsKey(model.getChargeOn1().toString())) {
							strChargeList.add(model.getChargeOn1().toString());
							chargeMap.put(model.getChargeOn1().toString(), model);
						}
					}
					if (model.getChargeOn2() != null) {
						if (!chargeMap.containsKey(model.getChargeOn2().toString())) {
							strChargeList.add(model.getChargeOn2().toString());
							chargeMap.put(model.getChargeOn2().toString(), model);
						}
					}
					if (log.isDebugEnabled()) {
						// log.debug("getSelectedLovAttribute.size() = " +
						// formBean.getSelectedLovAttribute().size());
						log.debug("model.getChargeOn1().toString() = "
								+ (model.getChargeOn1() == null ? "" : model.getChargeOn1().toString()));
					}
				}

			}
			// chargeMap.clear();
			// US-GER Product Group Sanjeevi start
			List<String> subtractList = compareChargeList(strChargeList, avbList);
			// US-GER Product Group Sanjeevi end
			if (subtractList.size() > 0) {
				String chargeMethod = "";
				String chargeAttribute = "";
				for (String chargeId : subtractList) {
					ChargeMethodPendingModel model = chargeMap.get(chargeId);
					if (StringUtils.hasLength(chargeAttribute)) {
						chargeMethod = chargeMethod + "," + model.getChargeCode();
					} else {
						chargeMethod = model.getChargeCode();
					}

					String chargeDesc = "";
					if (formBean.getChargeOnMap().containsKey(chargeId)) {
						chargeDesc = formBean.getChargeOnMap().get(chargeId);
					}
					if (StringUtils.hasLength(chargeAttribute)) {
						chargeAttribute = chargeAttribute + "," + chargeDesc;
					} else {
						chargeAttribute = chargeDesc;
					}
				}
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGEMETHODLIST,
						new Object[] { chargeAttribute, chargeMethod });
				formBean.setSelectedChargeOnList(formBean.getBkSelectedChargeOnList());
				return null;
			}
		}
		/*
		 * for (String selectedCharge : formBean.getSelectedChargeOnList()) {
		 * if(log.isDebugEnabled()){ log.debug("selectedCharge = " + selectedCharge); }
		 * }
		 */
		if (log.isDebugEnabled()) {
			log.debug("getSelectedChargeOnList.size() = " + formBean.getSelectedChargeOnList().size());
			log.debug("formBean.isGenerateAllowed()   = " + formBean.getIsGenerateAllowed());
		}
		formBean.setBkSelectedChargeOnList(formBean.getSelectedChargeOnList());
		return null;
	}

	private String renderChargeOnLovList() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		formBean.populateChargeOnSelect(formBean.getCountryCode(), formBean.getInterfaceName(),
				formBean.getProductType());
		return "abc";
	}

	public String doSearch() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		// formBean.resetList();
		if (log.isDebugEnabled()) {
			log.debug("start:  formBean = " + formBean);
		}
		formBean.setSearchResultList(null);
		if (formBean.getCountryCode() == null) {
			formBean.setCountryCode("");
		}
		if (formBean.getSearchCountryCode() == null) {
			formBean.setSearchCountryCode("");
		}
		List<GlobalProductMappingVO> globalList = null;

		ProductModel model = new ProductModel();
		model.setInterfaceName(formBean.getSearchInterfaceName().trim());
		model.setProductCategory(formBean.getSearchProductCategory().trim());
		model.setMainProduct(formBean.getSearchMainProduct().trim());
		model.setProductCode(formBean.getSearchProductCode().trim());

		List<ProductDefinitionVO> list = null;

		// Global Product Mapping
		if ((formBean.getSearchGlobalMainProduct() != null
				&& StringUtils.hasLength(formBean.getSearchGlobalMainProduct().trim()))
				|| (formBean.getSearchGlobalMainProductDescription() != null
						&& StringUtils.hasLength(formBean.getSearchGlobalMainProductDescription().trim()))
				|| (formBean.getSearchGlobalProductCategory() != null
						&& StringUtils.hasLength(formBean.getSearchGlobalProductCategory().trim()))
				|| (formBean.getSearchGlobalProductCode() != null
						&& StringUtils.hasLength(formBean.getSearchGlobalProductCode().trim()))
				|| (formBean.getSearchGlobalProductDescription() != null
						&& StringUtils.hasLength(formBean.getSearchGlobalProductDescription().trim()))) {

			globalList = productDefinitionService.getSearchGlobalProdList(formBean.getSearchGlobalMainProduct(),
					formBean.getSearchGlobalMainProductDescription(), formBean.getSearchGlobalProductCategory(),
					formBean.getSearchGlobalProductCode(), formBean.getSearchGlobalProductDescription(),
					formBean.getSearchCountryCode());
			if (globalList != null && !globalList.isEmpty()) {

				list = (List<ProductDefinitionVO>) productDefinitionService.getSearchProductList(
						formBean.getSearchCountryCode().trim(), formBean.getSearchProductCode().trim(),
						formBean.getSearchProductCodeDescription().trim(), formBean.getSearchInterfaceName().trim(),
						formBean.getSearchProductCategory().trim(), formBean.getSearchMainProduct().trim(), true,
						userContext.getSelectedBusinessSegment(), false, null, null);
				List<ProductDefinitionVO> tempList = new ArrayList<ProductDefinitionVO>();
				if (list != null && !list.isEmpty()) {
					for (ProductDefinitionVO prodcod : list) {
						for (GlobalProductMappingVO globalProductMappingVO : globalList) {
							if (org.apache.commons.lang.StringUtils.equalsIgnoreCase(prodcod.getProductCode(),
									globalProductMappingVO.getGlobalProductCode())) {
								tempList.add(prodcod);

							}
						}

					}
				}
				if (tempList.isEmpty() && list != null && !list.isEmpty()) {
					list = new ArrayList<ProductDefinitionVO>();
				}
				if (!tempList.isEmpty()) {
					list = new ArrayList<ProductDefinitionVO>();
					list = tempList;
				}
			}
		} else {
			list = (List<ProductDefinitionVO>) productDefinitionService.getSearchProductList(
					formBean.getSearchCountryCode().trim(), formBean.getSearchProductCode().trim(),
					formBean.getSearchProductCodeDescription().trim(), formBean.getSearchInterfaceName().trim(),
					formBean.getSearchProductCategory().trim(), formBean.getSearchMainProduct().trim(), true,
					userContext.getSelectedBusinessSegment(), false, null, null);

		}
		if (list == null) {
			list = new ArrayList<ProductDefinitionVO>();
		}
		formBean.setSearchResultList(list);
		if (super.validateSearchResult(formBean)) {
			formBean.setFocus("productDefinitionSearchDataTable");
		}
		if (log.isDebugEnabled()) {
			log.debug("List size=" + (list == null ? 0 : list.size()));
			log.debug("end: formBean = " + formBean);
		}
		return null;
	}

	@Override
	public Long getFunctionId() {
		return BaseConstants.FunctionId.PRODUCT_DEF.value();
	}

	/**
	 * Validates user's access right to specific module. Used by onload-config.xml
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	@Override
	public String validateUserAccessRight() throws Exception {
		final String action = super.getParameter("action");
		if (log.isDebugEnabled()) {
			log.debug(new StringBuilder().append("start: action = ").append(action));
		}

		if (action != null && action.equals(ClientAction.NEW.value())) {
			this.doNew();
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		String toViewId = null;
		toViewId = FunctionUtil.hasACL(userContext, formBean, this.getFunctionId());
		toViewId = FunctionUtil.hasACL(userContext,
				(ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME), this.getFunctionId());
		toViewId = FunctionUtil.hasACL(userContext, (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME),
				this.getFunctionId());

		toViewId = super.validateSessionTimeOut(toViewId);
		if (log.isDebugEnabled()) {
			log.debug(new StringBuilder().append("return: toViewId = ").append(toViewId).append(", formBean = [")
					.append(formBean).append("]"));
		}
		return toViewId;
	}

	public void doGenerateCartesianTable() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("doGenerateCartesianTable() -- start");
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("doGenerateCartesianTable() -- formBean:" + formBean);
		}
		List<ProductLovModel> lovAttributeList = formBean.getProductLovList();
		List<ProductPendingModel> lovList = null;

		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			// formBean.setDefSelectedLOVAttributesList(formBean.getSelectedLOVAttributesList());
		} else {
			lovList = formBean.getDefaultLovProductList();
		}
		formBean.setGenerateNeeded(false);
		// Query service to get the values and return a 2-dimensional array,
		// lovAttributeList as param.

		String[][] lovArray = productDefinitionService.getLOVValuesList(lovAttributeList);

		if (lovArray != null && lovArray.length > 0) {
			if (log.isDebugEnabled()) {
				log.debug("doGenerateCartesianTable() -- lovArray.size():" + (lovArray == null ? 0 : lovArray.length));
				log.debug("doGenerateCartesianTable() -- lovAttributeList.size():"
						+ ((lovAttributeList == null) ? 0 : lovAttributeList.size()));
			}
			String[][] cartesianTableArray = CartesianTable.getCartesianProductArray(lovArray);
			String[][] finalCartesianTableArray = new String[cartesianTableArray.length - 1][];
			System.arraycopy(cartesianTableArray, 0, finalCartesianTableArray, 0, finalCartesianTableArray.length);

			SubProductVO[] lovProductsArray = productDefinitionService.getSubProductsList(formBean.getCountryCode(),
					formBean.getInterfaceName(), formBean.getProductCode(), finalCartesianTableArray, lovAttributeList,
					lovList);

			formBean.setCartesianArray(finalCartesianTableArray);
			formBean.setColumnCount(lovArray.length);
			formBean.setLovProductsArray(lovProductsArray);
			// formBean.setDefaultLovProductsArray(lovProductsArray);
			if (lovProductsArray != null) {
				if (log.isDebugEnabled()) {
					log.debug("size of sub products array" + (lovProductsArray == null ? 0 : lovProductsArray.length));
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("column count=" + ((lovArray == null) ? 0 : lovArray.length));
			}
		}

		// Refresh the product list in Charge Code and Charge Method.
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		chargeBean.setProductCodeList();

		MethodBean chargeMethodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		chargeMethodBean.setProductCodeList();

		if (log.isDebugEnabled()) {
			log.debug("doGenerateCartesianTable() -- end");
		}
	}

	public void doClearRadioNonGpbsProduct() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		formBean.setIsEnabNonGpbsProdType(null);
		formBean.setNonGpbsPrdType(null);
		formBean.setNonGpbsRemarks(null);
		formBean.setIsRemarksEnable(Boolean.FALSE);
	}

	@SuppressWarnings("java:S1541")
	private boolean validateRule(ProductDefinitionBean productBean) {

		boolean isValid = true;
		RuleBean ruleBean = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);

		List<RulesPendingModel> ruleList = ruleBean.getSubRulesList();
		// Rules is mandatory for all the products except Manual Product.
		if ((productBean.getInterfaceName() != null && productBean.getInterfaceName().equals(EFBS_MANUAL_INTERFACE))
				|| (productBean.getProductType() != null
						&& ProductType.INTEREST.value().equalsIgnoreCase(productBean.getProductType()))
						&& !ProductType.PENALTY.value().equalsIgnoreCase(productBean.getProductType())) {
			if (ruleBean.getRuleType() != null && ruleBean.getRuleType().equals(RuleCategory.BASIC.toString())
					&& (ruleList != null && ruleList.size() > 0)) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_RULEEXISTFORMANUAL);
			} else if (ruleBean.getRuleType() != null && ruleBean.getRuleType().equals(RuleCategory.EXPR.toString())
					&& (ruleBean.getExpressionFuncRuleInId() != null
							&& ruleBean.getExpressionFuncRuleInId().trim().length() > 0)) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_RULENOTEXIST);
			}
		} else if (productBean.getProductLevel() != null
				&& ProductLevel.CROSSPRODUCT.value().equalsIgnoreCase(productBean.getProductLevel())) {
			if (ruleBean.getRuleType() == null && ruleList == null) {
				// Added by Anand for US&DE - Cross Product - Starts
				RuleBean ruleBeanCP = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
				ProductDefinitionBean proDefBean = (ProductDefinitionBean) super.getManagedBean(
						RuleBean.BACKING_BEAN_NAME);

				ruleBeanCP.resetRules();
				try {
					proDefBean.resetLov();
				} catch (Exception e) {
					log.error(e);
				}
				// Added by Anand for US&DE - Cross Product - Ends

			}
		} else if (productBean.getInterfaceName() != null
				&& !productBean.getInterfaceName().equals(EFBS_MANUAL_INTERFACE)
				&& !ProductType.PENALTY.value().equalsIgnoreCase(productBean.getProductType())) {
			if (ruleBean.getRuleType() != null && ruleBean.getRuleType().equals(RuleCategory.BASIC.toString())
					&& (ruleList == null || ruleList.size() == 0)) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_RULENOTEXIST);
			} else if (ruleBean.getRuleType() != null && ruleBean.getRuleType().equals(RuleCategory.EXPR.toString())
					&& (ruleBean.getExpressionFuncRuleInId() == null
							|| ruleBean.getExpressionFuncRuleInId().trim().length() == 0)) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_RULENOTEXIST);
			}
			// PBI000000193850 [S]
			if (productBean.getProductLevel() != null
					&& ProductLevel.VOL.value().equalsIgnoreCase(productBean.getProductLevel())
					&& ruleBean.getRuleType() != null
					&& !RuleCategory.BASIC.toString().equalsIgnoreCase(ruleBean.getRuleType())) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_INVALID_RULETYPE);
			}
			// PBI000000193850 [E]
		}

		if (FacesUtil.getMessageCount() > 0) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * added by ectan@20090603 - HK-SIT defect [35] validate charge code definition
	 * tab
	 * 
	 */
	private boolean validateChargeCodeDefinition() {
		boolean isValid = true;
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		ProductDefinitionBean productBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		List<ChargeCodePendingModel> chargeCodeList = chargeBean.getChargeCodeList();

		if (chargeCodeList == null || chargeCodeList.size() == 0) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGECODENOTEXIST);
		} else {
			if (StringUtils.hasLength(productBean.getTxnNarrative1())) {
				if (productBean.getTxnNarrative1().toString()
						.contains(BaseConstants.NARRATIVE_SHORTDESCRIPTION.toString())
						|| productBean.getTxnNarrative1().toString()
								.contains(BaseConstants.NARRATIVE_SHORTDESCRIPTION_CRD.toString())) {
					for (ChargeCodePendingModel chargeCodePendingModel : chargeCodeList) {
						if (StringUtils.hasLength(chargeCodePendingModel.getShortDescription())) {
							if (!ValidatorUtil.isSpecialChar(chargeCodePendingModel.getShortDescription())) {
								FacesUtil.addErrorMessage(null, MessageConstants.ERROR_INVALID,
										new Object[] { FacesUtil.getMessageByKey(
												MessageConstants.CHARGEPARAM_CHARGECODE_LABEL_CHARGESHORTDESCRIPTION) });
							}
						}

						if (!StringUtils.hasLength(chargeCodePendingModel.getShortDescription())) {
							FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED,
									new Object[] { FacesUtil.getMessageByKey(
											MessageConstants.CHARGEPARAM_CHARGECODE_LABEL_CHARGESHORTDESCRIPTION) });
						}
					}
				}
			}
		}

		// GPBS-394 [S]
		for (ChargeCodePendingModel chargeCodePendingModel : chargeCodeList) {
			if (StringUtils.hasLength(chargeCodePendingModel.getChargeCode())
					&& (chargeCodePendingModel.getChargeCode().toUpperCase().endsWith("NOVAT")
							|| chargeCodePendingModel.getChargeCode().toUpperCase().endsWith("NOTAX"))
					&& StringUtils.hasLength(chargeCodePendingModel.getTaxTypeCode())) {
				FacesUtil.addErrorMessage(null, MessageConstants.CHARGEPARAM_CHARGECODE_ERROR_TAXTYPECODE_INVALID,
						new Object[] { chargeCodePendingModel.getChargeCode() });
			}
		}
		// GPBS-394 [E]
		if (FacesUtil.getMessageCount() > 0) {
			isValid = false;
		}

		return isValid;
	}

	/**
	 * added by ectan@20090603 - HK-SIT defect [35] validate charge method tab
	 * 
	 */
	private boolean validateChargeMethod() {
		boolean isValid = true;
		MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		List<ChargeMethodPendingModel> subMethodList = methodBean.getSubMethodList();

		if (subMethodList == null || subMethodList.size() == 0) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_CHARGEMETHODNOTEXIST);
		}

		if (FacesUtil.getMessageCount() > 0) {
			isValid = false;
		}

		return isValid;
	}

	private boolean validatePLAccount() {
		boolean isValid = true;
		PandLSetupBean pandLSetupBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		List<ProductPLAccountPendingModel> subPLAccountList = pandLSetupBean.getSubPLAccountList();

		if (subPLAccountList == null || subPLAccountList.size() == 0) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_PANDLACCOUNTNOTEXIST);
		}

		if (FacesUtil.getMessageCount() > 0) {
			isValid = false;
		}

		return isValid;
	}

	private boolean validatePLAccountBranchCode() {

		boolean isValid = true;
		boolean isAllBrachFailure = true;
		PandLSetupBean pandLSetupBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		List<ProductPLAccountPendingModel> subPLAccountList = pandLSetupBean.getSubPLAccountList();
		Map<String, List<String>> multimap = new HashMap<>();
		for (int i = 0; i < subPLAccountList.size(); i++) {
			List<String> branchCurrencyCode = new ArrayList<String>();
			String branchCode = subPLAccountList.get(i).getBranchCode();
			String chargeCode = subPLAccountList.get(i).getChargeCode();
			String currencyCode = subPLAccountList.get(i).getCurrencyCode();
			if (multimap.containsKey(chargeCode)) {
				List<String> value = multimap.get(chargeCode);
				value.add(branchCode + "_" + currencyCode);
				multimap.put(chargeCode, value);
			} else {
				branchCurrencyCode.add(branchCode + "_" + currencyCode);
				multimap.put(chargeCode, branchCurrencyCode);
			}

		}

		for (ProductPLAccountPendingModel model : subPLAccountList) {
			List<String> branchCurrenciesList = multimap.get(model.getChargeCode());
			if (!branchCurrenciesList.contains("ALL" + "_" + "999")) {
				isAllBrachFailure = false;
				break;
			}

		}

		if (!isAllBrachFailure) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_BRANCHCODEALLALLNOTEXIST);
		}

		if (FacesUtil.getMessageCount() > 0) {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * Performs insert/update function, and then save the record as draft.
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	@SuppressWarnings("java:S1541")
	public String doSaveAsDraft() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start");
		}

		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);

		if (!formBean.validate()) {
			return null;
		}

		if (!this.validateRule(formBean)) {
			return null;
		}

		if (StringUtils.hasText(formBean.getPenaltyType())) {
			if (!formBean.validatePenaltyTypeRule(formBean.getCountryCode(), formBean.getPenaltyType())) {
				return null;
			}
		}

		// start added by ectan@20090603
		if (!this.validateChargeCodeDefinition()) {
			return null;
		}

		if (!this.validateChargeMethod()) {
			return null;
		}
		// added by GiriPrasad
		if (!this.validatePLAccount()) {
			return null;
		}
		// end added by ectan@20090603
		// added by Ramadevi for product mapping
		if (!this.validateProductCharge()) {
			return null;
		}

		if (!this.validateProductChrgeMethodAndChargeOn()) {
			return null;
		}

		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())
				&& !validateProductCode(formBean.getCountryCode(), formBean.getInterfaceName(),
						formBean.getProductCode())) {
			return null;
		}
		// modify the message
		if (formBean.isGenerateNeeded()) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_GENERATENEEDED);
			return null;
		}
		String keyAttributeLabel = formBean.getKeyAttributeLabel();
		if (this.processMakerAction(ClientAction.SAVE_AS_DRAFT.value(), keyAttributeLabel)) {
			FacesUtil.addInfoMessage(null, MessageConstants.MSG_SAVEDRAFT_SUCCESS, new Object[] { keyAttributeLabel });

			if (formBean.getTxnProductGrouping()) {
				FacesUtil.addWarnMessage(null, MessageConstants.FEE_TXN_PRD_GROUPING_ENABLED_WARN_MSG, new Object[] {});
			}
			// mainForm.reset();
		}
		if (log.isDebugEnabled()) {
			log.debug("end");
		}
		return null;
	}

	/**
	 * Performs insert/update function, and then submit the record for
	 * creation/modification approval.
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	@SuppressWarnings("java:S1541")
	public String doSaveAsSubmit() throws Exception {

		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);

		if (log.isDebugEnabled()) {
			log.debug("start");
		}

		if (!formBean.validate()) {
			return null;
		}

		if (!this.validateRule(formBean)) {
			return null;
		}

		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {

			if (StringUtils.hasText(formBean.getPenaltyType())) {
				if (!formBean.validatePenaltyTypeRule(formBean.getCountryCode(), formBean.getPenaltyType())) {
					return null;
				}
			}
		}

		// start added by ectan@20090603
		if (!this.validateChargeCodeDefinition()) {
			return null;
		}

		if (!this.validateChargeMethod()) {
			return null;
		}

		if (!this.validateBicBasedProduct()) {
			return null;
		}

		if (formBean.getOnlineFlag() == Boolean.TRUE) {
			if (!this.validatePLAccount()) {
				return null;
			}
			if (!this.validatePLAccountBranchCode()) {
				return null;
			}
		}

		if (!this.validateSelectedChargeOn()) {
			return null;
		}
		// added by Ramadevi for product mapping for tax and non-tax changes
		if (!this.validateProductCharge()) {
			return null;
		}

		if (!this.validateProductChrgeMethodAndChargeOn()) {
			return null;
		}

		// added by srikanth
		if (!formBean.validateProductPriority(formBean.getProductDerivePriority())) {
			return null;
		}

		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())
				&& !validateProductCode(formBean.getCountryCode(), formBean.getInterfaceName(),
						formBean.getProductCode())) {
			return null;
		}

		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())
				&& StringUtils.hasLength(formBean.getDescription().trim())) {
			ProductModel prodModel = this.productDefinitionService.getProductDescription(formBean.getDescription(),
					formBean.getCountryCode());
			if (prodModel != null) {
				FacesUtil.addErrorMessage(null, MessageConstants.ERROR_DUPLICATE_PRODUCT_DESCRIPTION_MSG, null);
				return null;
			}
		}

		// modify the message
		if (formBean.isGenerateNeeded()) {
			FacesUtil.addInfoMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_GENERATENEEDED, null);
			return null;
		}
		// Added for Trade Sprint3 changes by Ramadevi T
		if (formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())
				&& validateQuarterlyProductCode(formBean.getCountryCode(), formBean.getBusinessSegment(),
						formBean.getProductCode(), formBean.getLowestFrequency())) {

			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCT_QUARTERLY_PRODUCT_CAN_NOT_MODIFY, null);
			return null;

		}
		// GPBS-394 [S]
		if (StringUtils.hasLength(formBean.getProductLevel())
				&& formBean.getProductLevel().equals(ProductLevel.VOL.value())
				&& !formBean.getChargePassThroughFlag()) {
			FacesUtil.addWarnMessage(null, MessageConstants.PRODUCTDEFINITION_WARN_VOL_CHARGE_PASSTHROUGH_YES, null);
		}
		// GPBS-394 [E]
		String keyAttributeLabel = formBean.getKeyAttributeLabel();
		if (this.processMakerAction(ClientAction.SAVE_AS_SUBMIT.value(), keyAttributeLabel)) {
			FacesUtil.addInfoMessage(null, MessageConstants.MSG_SAVESUBMIT_SUCCESS, new Object[] { keyAttributeLabel });
			if (formBean.getTxnProductGrouping()) {
				FacesUtil.addWarnMessage(null, MessageConstants.FEE_TXN_PRD_GROUPING_ENABLED_WARN_MSG, new Object[] {});
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("end");
		}
		return null;
	}

	/**
	 * Saves the pending record with specific status.
	 * 
	 * @param action the action
	 * @throws Exception if any error occurs
	 */
	private boolean processMakerAction(final String action, final String keyAttributeLabel) throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("start: action = " + action + ", pendingId = " + formBean.getPendingId() + ", formBean = "
					+ formBean + "]");
		}
		if (!action.equals(ClientAction.SAVE_AS_DRAFT.value()) && !action.equals(ClientAction.SAVE_AS_SUBMIT.value())
				&& !action.equals(ClientAction.DELETE.value())) {
			throw new Exception("[Product Definition] Invalid maker action = " + action + ", pendingId = "
					+ formBean.getPendingId());
		}

		ProductModelPK prodPK = new ProductModelPK();
		prodPK.setCountryCode(formBean.getCountryCode());
		// prodPK.setInterfaceName(formBean.getInterfaceName());
		prodPK.setProductCode(formBean.getProductCode());
		// change to validate product code is only country specific exculding
		// interface
		/*
		 * ProductModel productMaster = productDefinitionService .getProduct(prodPK);
		 */
		ProductModel productMaster = productDefinitionService.getProductIgnoreInterface(prodPK.getCountryCode(),
				prodPK.getProductCode(), false);

		if (action.equals(ClientAction.SAVE_AS_DRAFT.value()) || action.equals(ClientAction.SAVE_AS_SUBMIT.value())) {
			if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value()) && productMaster != null) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_PRODUCT_LABEL_DUPLICATE,
						new Object[] { prodPK.getProductCode() });
				return false;
			}
		} else if (action.equals(ClientAction.DELETE.value())) {
			if (productMaster == null) {
				FacesUtil.addErrorMessage(null, MessageConstants.MSG_MASTERRECORD_NOTFOUND,
						new Object[] { keyAttributeLabel });
				return false;
			}
		}

		/*
		 * if(formBean.getMaintenanceStep().equals( MaintenanceStep.New.value())){
		 * productMaster = new ProductModel(); BeanUtils.copyProperties(formBean,
		 * productMaster); }
		 */

		//
		// INSERT PARENT TABLE AND CHILD TABLE
		// PRODUCT_CHARGE select item
		// PRODUCT_LOV select item
		// chargeCode table
		// chargeMethod table
		// RULES
		// GET CARTESION VALUES..........HOW?????
		ProductPendingModel parentPending = new ProductPendingModel();
		// CheckerMakerVO checkerMakerVO = copyForm2MakerVO(action, formBean);
		CheckerMakerVO checkerMakerVO = new CheckerMakerVO();

		if (!generateProcessMakerVO(action, productMaster, parentPending, checkerMakerVO, keyAttributeLabel)) {
			return false;
		}

		if (log.isDebugEnabled()) {
			log.debug("saving: checkerMakerVO = [" + checkerMakerVO + "]");
		}

		// try {
		if (action.equals(ClientAction.SAVE_AS_DRAFT.value())) {
			this.productDefinitionService.saveAsDraft(checkerMakerVO, this.getModuleId(), getCurrentUserId(), true,
					false, null);
			// String pendingId = formBean.getPendingId().toString();
			// log.debug("pendingId =" + pendingId);
			// this.doNew();
			// getPendingDetails(formBean, pendingId);
		} else if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())) {
			this.productDefinitionService.saveAndSubmit(checkerMakerVO, this.getModuleId(), getCurrentUserId(), true,
					false, null);
			// setPopulatePrimaryKeyFields();
		} else if (action.equals(ClientAction.DELETE.value()) && !formBean.hasPendingId()) {
			this.productDefinitionService.saveAndSubmit(checkerMakerVO, this.getModuleId(), getCurrentUserId(), true,
					false, null);
			// setPopulatePrimaryKeyFields();
		} else {
			throw new Exception(
					"[PRODUCT] Invalid maker action = " + action + ", pendingId = " + formBean.getPendingId());
		}

		BasePAModel base = checkerMakerVO.getParent();
		if (log.isDebugEnabled()) {
			log.debug("pendingId    =" + base.getPendingId());
		}
		// this.doReadPending(base.getPendingId().toString());
		this.setCheckerMakerValues(base, formBean);

		// formBean.copyPendingModel2Form(userPending);
		// } catch (BusinessException be) {
		// if
		// (be.getErrorCode().equals(CheckerMakerService.ERR_SAME_REC_PENDING_APPRORAL))
		// {
		// FacesUtil.addErrorMessage(null,
		// MessageConstants.MSG_SAVESUBMIT_DUPLICATED,
		// new Object[] { keyAttributeLabel });
		// return false;
		// } else if (be.getErrorCode().equals(
		// CheckerMakerService.ERR_SAME_REC_DRAFTED_FOR_SAME_MAKER)) {
		// FacesUtil.addErrorMessage(null,
		// MessageConstants.MSG_SAVEDRAFT_DUPLICATED,
		// new Object[] { keyAttributeLabel });
		// return false;
		// }
		// }
		this.doReadPending(formBean.getPendingId().toString());
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = " + formBean);
		}
		return true;
	}

	private void setCheckerMakerValues(BasePAModel base, ProductDefinitionBean formBean) throws Exception {
		formBean.setPendingId(base.getPendingId());
		formBean.setMakerId(base.getMakerId());
		formBean.setMakerDate(base.getMakerDate());
		formBean.setCheckerId(base.getCheckerId());
		formBean.setCheckerDate(base.getCheckerDate());
		formBean.setStatus(base.getStatus());
		formBean.setMaintenanceStep(MaintenanceStep.Modify.value());
		setPopulatePrimaryKeyFields();
	}

	private boolean generateProcessMakerVO(final String action, ProductModel productMaster,
			ProductPendingModel parentPending, CheckerMakerVO checkerMakerVO, final String keyAttributeLabel)
			throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		ProductChargeActionBean prodChargeAction = (ProductChargeActionBean) super.getManagedBean(
				ProductChargeActionBean.BACKING_BEAN_NAME);
		// MethodBean methBean = (MethodBean)
		// super.getManagedBean(MethodBean.BACKING_BEAN_NAME);

		if (formBean.getPendingId() != null && formBean.getPendingId() > 0) {
			parentPending = this.productDefinitionService.getProductByPendingId(formBean.getPendingId());
			if (parentPending == null) {
				FacesUtil.addErrorMessage(null, MessageConstants.ERROR_NOTFOUND, new Object[] {
						FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID), formBean.getPendingId() });
				return false;
			} else {
				// BeanUtils.copyProperties(formBean, parentPending); remarked by ectan@20090612
				// - standardize the code
				// (hk sit defect [4])

				// start added by ectan@20090612 - standardize the code (hk sit defect [4])

				parentPending.setBranchCode(formBean.getBranchCode());
				parentPending.setChargeRebateFlag(formBean.getChargeRebateFlag());
				parentPending.setCheckerDate(formBean.getCheckerDate());
				parentPending.setCheckerId(formBean.getCheckerId());
				parentPending.setClientTimeZone(formBean.getClientTimeZone());
				parentPending.setCountryCode(formBean.getCountryCode());
				parentPending.setCreatedDate(formBean.getCreatedDate());
				parentPending.setDescription(formBean.getDescription());
				parentPending.setGlobalProductFlag(false);
				parentPending.setGlobalProductCode(formBean.getGlobalProductCode());
				parentPending.setAfpCode(formBean.getAfpCode());
				parentPending.setMaskAmtFlag(formBean.getMaskAmtFlag());
				parentPending.setBackdatedPSGLFlag(formBean.getBackdatedPSGLFlag());
				parentPending.setHierarchyValue(formBean.getHierarchyValue());
				parentPending.setInterfaceName(formBean.getInterfaceName());
				parentPending.setInvDescLocalLang(formBean.getInvDescLocalLang());
				parentPending.setInvoiceDescription(formBean.getInvoiceDescription());
				parentPending.setLowestEntityLevel(formBean.getLowestEntityLevel());
				parentPending.setLowestFrequency(formBean.getLowestFrequency());
				parentPending.setMainProduct(formBean.getMainProduct());
				parentPending.setMakerDate(formBean.getMakerDate());
				parentPending.setMakerId(formBean.getMakerId());
				parentPending.setPendingId(formBean.getPendingId());
				parentPending.setPrecedence(formBean.getPrecedence());
				parentPending.setProductCategory(formBean.getProductCategory());
				parentPending.setProductCode(formBean.getProductCode());
				parentPending.setProductLevel(formBean.getProductLevel());
				parentPending.setProductType(formBean.getProductType());
				parentPending.setRuleId(formBean.getRuleId());
				parentPending.setStatus(formBean.getStatus());
				parentPending.setOnlineFlag(formBean.getOnlineFlag());
				parentPending.setScpayProduct(formBean.getScpayProduct());
				// 99 back log changes by Ramadevi T
				parentPending.setNonGpbsPrdType(formBean.getNonGpbsPrdType());
				parentPending.setNonGpbsRemarks(formBean.getNonGpbsRemarks());
				parentPending.setPenaltyCtg(formBean.getPenaltyType());

				// Added by Sudhakar J for Transaction Grouping
				parentPending.setTxnProductGrouping(formBean.getTxnProductGrouping());

				if (formBean.getProductDerivePriority() != null
						&& StringUtils.hasLength(formBean.getProductDerivePriority().toString()))
					parentPending
							.setProductDerivePriority(Integer.valueOf(formBean.getProductDerivePriority().toString()));

				// R.A.R DotOPAL CR -- Check if Backdated Product
				// ChargeLevel = SUB is not applicable for MANUAL interface, CrossProducts,
				// OneTime, and products
				// without rules.
				if (formBean.getBackdatedPSGLFlag()) {
					if (formBean.getInterfaceName() != null
							&& !(formBean.getInterfaceName().equals(BaseConstants.EFBS_MANUAL_INTERFACE)
									&& BaseConstants.PRODUCT_LEVEL_VOLUME.equals(formBean.getProductLevel()))
							&& formBean.getLowestFrequency() != null
							&& (!formBean.getProductLevel().equals(BaseConstants.PRODUCT_LEVEL_CROSS_PRODUCT)
									&& !(formBean.getProductLevel().equals(BaseConstants.PRODUCT_LEVEL_VOLUME)
											&& (formBean.getLowestFrequency()
													.equals(BaseConstants.Frequency.ONETIME.value()))))) {
						// do nothing...
					} else {
						FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_BACKDATEDPSGLPOSTING,
								null);
						return false;
					}
				}
				// R.A.R

				if (StringUtils.hasLength(formBean.getUnitCost())) {
					if (ValidatorUtil.IsDecimal(formBean.getUnitCost())) {
						parentPending.setUnitCost(new BigDecimal(formBean.getUnitCost()));
					} else {
						FacesUtil.addErrorMessage(null, MessageConstants.ERROR_INVALID, new Object[] {
								FacesUtil.getMessageByKey(MessageConstants.PRODUCTDEFINITION_UNITCOST) });
					}
				}
				// for #cr112 by cheah
				if (formBean.getRepetitiveLimit() != null && StringUtils.hasLength(formBean.getRepetitiveLimit())) {
					parentPending.setRepetitiveLimit(new BigDecimal(formBean.getRepetitiveLimit()));
				}
				parentPending.setUpdatedDate(formBean.getUpdatedDate());
				parentPending.setWbdwProductCode(formBean.getWbdwProductCode());
				parentPending.setWbdwProductCodeDesc(formBean.getWbdwProductCodeDesc());
				// end added by ectan@20090612 - standardize the code (hk sit defect [4])
				// Start - DPS
				parentPending.setDpsProductCode(formBean.getDpsProductCode());
				parentPending.setDpsProductType(formBean.getDpsProductType());
				// end - DPS
				// Start - CR#16
				parentPending.setIsShowChargeOnAsVol(formBean.getShowChargeOnAsVolumeFlag());
				parentPending.setVolChargeOnAttrId(formBean.getVolumeChargeOnAttribute());
				// end - CR#16
				// Start - CR#VolProduct_Derivation_GroupB
				// Start - LK CEFTS Product Definition Rule Creation and Modification Issue Fix
				// done on 02/02/2016 updated by ViswanathanVP
				if ((BaseConstants.ZERO).equals(formBean.getGroupByAttrId())) {
					parentPending.setGroupByAttrId(null);
				} else {
					parentPending.setGroupByAttrId(formBean.getGroupByAttrId());
				}
				// End - LK CEFTS Product Definition Rule Creation and Modification Issue Fix
				// done on 02/02/2016 updated by ViswanathanVP
				parentPending.setTxnNarrative1(formBean.getTxnNarrative1());
				// End - CR#VolProduct_Derivation_GroupBy
				// Start - CR#Volume Tiered Pricing
				parentPending.setTxnRollUpFlag(formBean.getTxnRollUpFlag());
				// End - CR#Volume Tiered Pricing
				parentPending.setShortDescription(formBean.getProdShortDescription());

				setChargePassThroughFlag(parentPending, formBean);
				setGlobalRebateFlag(parentPending, formBean);
				// MY FPX reversal
				setReversalFlag(parentPending, formBean);
			}
		} else {
			// copy from master table or pending table
			if (!formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
				BeanUtils.copyProperties(productMaster, parentPending);
			}
			if (!action.equals(ClientAction.DELETE.value())) {
				// copy possible updated fields
				// BeanUtils.copyProperties(formBean, parentPending); //remarked by
				// ectan@20090612 - standardize the
				// code (hk sit defect [4])

				// start added by ectan@20090612 - standardize the code (hk sit defect [4])
				parentPending.setBranchCode(formBean.getBranchCode());
				parentPending.setChargeRebateFlag(formBean.getChargeRebateFlag());
				parentPending.setCheckerDate(formBean.getCheckerDate());
				parentPending.setCheckerId(formBean.getCheckerId());
				parentPending.setClientTimeZone(formBean.getClientTimeZone());
				parentPending.setCountryCode(formBean.getCountryCode());
				parentPending.setCreatedDate(formBean.getCreatedDate());
				parentPending.setDescription(formBean.getDescription());
				parentPending.setGlobalProductFlag(false);
				parentPending.setGlobalProductCode(formBean.getGlobalProductCode());
				parentPending.setAfpCode(formBean.getAfpCode());
				parentPending.setMaskAmtFlag(formBean.getMaskAmtFlag());
				parentPending.setBackdatedPSGLFlag(formBean.getBackdatedPSGLFlag());
				parentPending.setHierarchyValue(formBean.getHierarchyValue());
				parentPending.setInterfaceName(formBean.getInterfaceName());
				parentPending.setInvDescLocalLang(formBean.getInvDescLocalLang());
				parentPending.setInvoiceDescription(formBean.getInvoiceDescription());
				parentPending.setLowestEntityLevel(formBean.getLowestEntityLevel());
				parentPending.setLowestFrequency(formBean.getLowestFrequency());
				parentPending.setMainProduct(formBean.getMainProduct());
				parentPending.setMakerDate(formBean.getMakerDate());
				parentPending.setMakerId(formBean.getMakerId());
				parentPending.setPendingId(formBean.getPendingId());
				parentPending.setPrecedence(formBean.getPrecedence());
				parentPending.setProductCategory(formBean.getProductCategory());
				parentPending.setProductCode(formBean.getProductCode());
				parentPending.setProductLevel(formBean.getProductLevel());
				parentPending.setProductType(formBean.getProductType());
				parentPending.setRuleId(formBean.getRuleId());
				parentPending.setStatus(formBean.getStatus());
				parentPending.setOnlineFlag(formBean.getOnlineFlag());
				parentPending.setScpayProduct(formBean.getScpayProduct());
				// non gpbs changes by Ramadevi T
				parentPending.setNonGpbsPrdType(formBean.getNonGpbsPrdType());
				parentPending.setNonGpbsRemarks(formBean.getNonGpbsRemarks());
				parentPending.setPenaltyCtg(formBean.getPenaltyType());

				// Added by Sudhakar J for Transaction Grouping
				parentPending.setTxnProductGrouping(formBean.getTxnProductGrouping());

				if (formBean.getProductDerivePriority() != null
						&& StringUtils.hasLength(formBean.getProductDerivePriority().toString()))
					parentPending
							.setProductDerivePriority(Integer.valueOf(formBean.getProductDerivePriority().toString()));

				// R.A.R DotOPAL CR -- Check if Backdated Product
				// ChargeLevel = SUB is not applicable for MANUAL interface, CrossProducts,
				// OneTime, and products
				// without rules.
				if (formBean.getBackdatedPSGLFlag()) {
					if (formBean.getInterfaceName() != null
							&& !(formBean.getInterfaceName().equals(BaseConstants.EFBS_MANUAL_INTERFACE)
									&& BaseConstants.PRODUCT_LEVEL_VOLUME.equals(formBean.getProductLevel()))
							&& formBean.getLowestFrequency() != null
							&& (!formBean.getProductLevel().equals(BaseConstants.PRODUCT_LEVEL_CROSS_PRODUCT)
									&& !(formBean.getProductLevel().equals(BaseConstants.PRODUCT_LEVEL_VOLUME)
											&& (formBean.getLowestFrequency()
													.equals(BaseConstants.Frequency.ONETIME.value()))))) {
						// do nothing...
					} else if (formBean.getInterfaceName() != null
							&& !(formBean.getInterfaceName().equals(BaseConstants.EFBS_MANUAL_INTERFACE)
									&& BaseConstants.PRODUCT_LEVEL_VOLUME.equals(formBean.getProductLevel()))
							&& formBean.getLowestFrequency() != null
							&& (!formBean.getProductLevel().equals(BaseConstants.PRODUCT_LEVEL_CROSS_PRODUCT)
									&& !(formBean.getProductLevel().equals(BaseConstants.PRODUCT_LEVEL_VOLUME)
											&& (formBean.getLowestFrequency()
													.equals(BaseConstants.Frequency.ONETIME.value()))))) {
						// do nothing...
					} else {
						FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_BACKDATEDPSGLPOSTING,
								null);
						return false;
					}
				}
				// R.A.R

				if (StringUtils.hasLength(formBean.getUnitCost())) {
					if (ValidatorUtil.IsDecimal(formBean.getUnitCost())) {
						parentPending.setUnitCost(new BigDecimal(formBean.getUnitCost()));
					} else {
						FacesUtil.addErrorMessage(null, MessageConstants.ERROR_INVALID, new Object[] {
								FacesUtil.getMessageByKey(MessageConstants.PRODUCTDEFINITION_UNITCOST) });
					}
				}
				// for #cr112 by cheah
				if (formBean.getRepetitiveLimit() != null && StringUtils.hasLength(formBean.getRepetitiveLimit())) {
					parentPending.setRepetitiveLimit(new BigDecimal(formBean.getRepetitiveLimit()));
				}

				parentPending.setUpdatedDate(formBean.getUpdatedDate());
				parentPending.setWbdwProductCode(formBean.getWbdwProductCode());
				parentPending.setWbdwProductCodeDesc(formBean.getWbdwProductCodeDesc());
				// end added by ectan@20090612 - standardize the code (hk sit defect [4])
				// Start - DPS
				parentPending.setDpsProductCode(formBean.getDpsProductCode());
				parentPending.setDpsProductType(formBean.getDpsProductType());
				// end - DPS
				// Start - CR#16
				parentPending.setIsShowChargeOnAsVol(formBean.getShowChargeOnAsVolumeFlag());
				parentPending.setVolChargeOnAttrId(formBean.getVolumeChargeOnAttribute());
				// end - CR#16
				// Start - CR#VolProduct_Derivation_GroupBy
				// Start - LK CEFTS Product Definition Rule Creation and Modification Issue Fix
				// done on 02/02/2016 updated by ViswanathanVP
				if ((BaseConstants.ZERO).equals(formBean.getGroupByAttrId())) {
					parentPending.setGroupByAttrId(null);
				} else {
					parentPending.setGroupByAttrId(formBean.getGroupByAttrId());
				}
				// End - LK CEFTS Product Definition Rule Creation and Modification Issue Fix
				// done on 02/02/2016 updated by ViswanathanVP
				parentPending.setTxnNarrative1(formBean.getTxnNarrative1());
				// End - CR#VolProduct_Derivation_GroupBy
				// Start - CR#Volume Tiered Pricing
				parentPending.setTxnRollUpFlag(formBean.getTxnRollUpFlag());
				// End - CR#Volume Tiered Pricing
				parentPending.setShortDescription(formBean.getProdShortDescription());

				setChargePassThroughFlag(parentPending, formBean);
				setGlobalRebateFlag(parentPending, formBean);
				// MY FPX reversal
				setReversalFlag(parentPending, formBean);
			}
			if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
				parentPending.setProductGroupLevel(BASE_PRODUCT_GROUP_LEVEL);
				parentPending.setRuleId(new Long(0));
				/*
				 * //TODO HARD CODE parentPending.setChargeRebateFlag("CHARGE");
				 */
			}
			if (log.isDebugEnabled()) {
				log.debug("formBean.getCountryCode() = " + formBean.getCountryCode());
				log.debug("formBean.getProductCategory() = " + formBean.getProductCategory());
			}
		}

		int status = parentPending.getStatus();
		if (log.isDebugEnabled()) {
			log.debug("formBean.getPendingId() = " + formBean.getPendingId());
			log.debug("status   = " + status);
		}
		parentPending.setMakerId(super.getCurrentUserId());
		parentPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
		parentPending.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
		if (log.isDebugEnabled()) {
			log.debug("productMaster   = " + productMaster);
			log.debug("parentPending   = " + parentPending);
		}
		parentPending.setBaseProductFlag(true);
		parentPending.setDisplayKeyValue(keyAttributeLabel);
		parentPending.setModuleId(this.getModuleId());
		// if businessSegment = 'CAS', set it to 'CASH'
		if (userContext.getSelectedBusinessSegment().equals(BusinessSegment.CAS.value())) {
			parentPending.setBusinessSegment(BusinessSegment.CASH.value());

		} else {
			parentPending.setBusinessSegment(userContext.getSelectedBusinessSegment());
		}

		// parentPending.set

		// to set the status and current version
		if (formBean.getPendingId() != null && formBean.getPendingId() > 0) {
			parentPending.setPendingId(formBean.getPendingId());
			parentPending.setStatus(status);
		} else if (productMaster != null) {
			parentPending.setStatus(productMaster.getStatus());
			parentPending.setBaseVersion(productMaster.getCurrentVersion());
		} else {
			parentPending.setStatus(BaseConstants.Status.UNSAVE.value());
			parentPending.setBaseVersion(new Long(0));
		}

		// to set the charge rebate flag
		if (parentPending.getProductType().equals(BaseConstants.ProductType.CHARGE.value())
				|| parentPending.getProductType().equals(BaseConstants.ProductType.PENALTY.value())) {
			parentPending.setChargeRebateFlag(PricingConstants.ChargeRebateFlag.CHARGE.value());
		} else {
			parentPending.setChargeRebateFlag(PricingConstants.ChargeRebateFlag.REBATE.value());
		}

		// to set the action type
		if (action.equals(ClientAction.SAVE_AS_DRAFT.value()) || action.equals(ClientAction.SAVE_AS_SUBMIT.value())) {
			// if (productMaster != null) {
			if ((formBean.getPendingId() != null && formBean.getPendingId() > 0) || productMaster != null) {
				parentPending.setActionType(ActionType.CHANGE.value());
			} else {
				parentPending.setActionType(ActionType.NEW.value());
			}
			// } else {
			// parentPending.setActionType(ActionType.NEW.value());
			// }
		} else if (action.equals(ClientAction.DELETE.value())) {
			parentPending.setActionType(ActionType.DELETE.value());
		} else {
			throw new Exception(
					"[PRODUCT] Invalid maker action = " + action + ", pendingId = " + formBean.getPendingId());
		}
		if (log.isDebugEnabled()) {
			log.debug("parentPending.getWbdwProductCodeDesc()=" + parentPending.getWbdwProductCodeDesc());
		}
		parentPending.setClientTimeZone(formBean.getClientTimeZone());
		// set parent
		checkerMakerVO.setParent(parentPending);

		List<BasePAModel> baseModelList = new ArrayList<BasePAModel>();

		// to set the child from charge code screen
		prodChargeAction.setChargeCodesToBeSaved(parentPending, baseModelList, formBean);

		if (!setChargeMethodChildVO(parentPending, action, baseModelList, formBean.getProductCode(), formBean)) {
			return false;
		}

		if (!setPLAccountChildVO(parentPending, action, baseModelList, formBean.getProductCode(), formBean)) {
			return false;
		}
		// set rules to be save as child of base product
		setRulesChildVO(baseModelList, parentPending, false);

		// set selected charge on attribute to be save as child of base product
		// save to pending product charge on table
		setProductChargeChildVO(action, formBean, baseModelList, parentPending, true);
		// set selected lov attribute to be save as child of base product
		// save to pending product lov table
		setProductLOVChildVO(action, formBean, baseModelList, parentPending);

		// Added by Srikanth For 4.4.1 multi language support -start
		prodChargeAction.setChargeCodeOtherLangInvDescChildVO(parentPending, baseModelList, formBean);
		setProdDefOtherLangInvDescChildVO(parentPending, action, baseModelList, formBean.getProductCode(), formBean);
		// Added for partner sharing
		prodChargeAction.setChargeCodePartnerSharingChildVO(parentPending, baseModelList, formBean);
		// ended
		// SET SUB PRODUCT VO AS CHILD
		// checkerMakerVO.setChildList(SUBPRODUCTVO);
		if (!setSubProductChildVO(action, baseModelList, formBean, parentPending, chargeBean, prodChargeAction)) {
			return false;
		}

		// set child list for the base product
		checkerMakerVO.setChildList(baseModelList);

		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
		return true;
	}

	private boolean setPLAccountChildVO(ProductPendingModel pendingModel, String action,
			List<BasePAModel> baseModelList, String productCode, ProductDefinitionBean formBean) throws Exception {
		if (action.equals(ClientAction.DELETE.value())) {
			List<ProductPLAccountModel> pandlList = productDefinitionService
					.getProductPLAccountList(pendingModel.getCountryCode(), pendingModel.getProductCode());
			if (pandlList != null) {
				ProductPLAccountPendingModel pandlPending = null;
				for (ProductPLAccountModel pandlModel : pandlList) {
					pandlPending = new ProductPLAccountPendingModel();
					BeanUtils.copyProperties(pandlModel, pandlPending);
					pandlPending.setBaseVersion(pandlModel.getCurrentVersion());
					pandlPending.setActionType(BaseConstants.ActionType.DELETE.value());
					pandlPending.setMakerId(super.getCurrentUserId());
					pandlPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(pandlPending);
				}
			}
		} else {
			ProductPLAccountActionBean pandlAction = (ProductPLAccountActionBean) super.getManagedBean(
					ProductPLAccountActionBean.BACKING_BEAN_NAME);
			if (!pandlAction.generatePLAccountVO(productCode, baseModelList, formBean)) {
				return false;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
		return true;
	}

	// to set the child from charge method screen
	private boolean setChargeMethodChildVO(ProductPendingModel pendingModel, String action,
			List<BasePAModel> baseModelList, String productCode, ProductDefinitionBean formBean) throws Exception {
		// List<ChargeMethodPendingModel> dummy = new
		// ArrayList<ChargeMethodPendingModel>();
		if (action.equals(ClientAction.DELETE.value())) {
			List<ChargeMethodModel> chargeMethodList = productDefinitionService.getChargeMethodList(
					pendingModel.getCountryCode(), pendingModel.getInterfaceName(), pendingModel.getProductCode(), "");
			if (chargeMethodList != null) {
				ChargeMethodPendingModel methodPending = null;
				for (ChargeMethodModel chargeMethodModel : chargeMethodList) {
					methodPending = new ChargeMethodPendingModel();
					BeanUtils.copyProperties(chargeMethodModel, methodPending);
					methodPending.setBaseVersion(chargeMethodModel.getCurrentVersion());
					methodPending.setActionType(BaseConstants.ActionType.DELETE.value());
					methodPending.setMakerId(super.getCurrentUserId());
					methodPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(methodPending);
				}
			}
		} else {
			ProductChargeActionBean prodChargeAction = (ProductChargeActionBean) super.getManagedBean(
					ProductChargeActionBean.BACKING_BEAN_NAME);
			if (!prodChargeAction.generateChargeMethodVO(productCode, baseModelList, formBean)) {
				return false;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
		return true;
	}

	// to set the child from rules screen
	private void setRulesChildVO(List<BasePAModel> baseModelList, ProductPendingModel pendingModel,
			boolean isLovChanged) throws Exception {
		RuleBean ruleBean = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
		List<RulesPendingModel> rulePending = ruleBean.getRulesToBeSaved(pendingModel, isLovChanged);
		if (log.isDebugEnabled()) {
			log.debug(">>>>>>>>>>>>rulePending=" + (rulePending == null ? 0 : rulePending.size()));
		}
		for (RulesPendingModel rulesPendingModel : rulePending) {
			rulesPendingModel.setRuleFrom(this.getModuleId());
			baseModelList.add(rulesPendingModel);
			if (log.isDebugEnabled()) {
				log.debug("rulesPendingModel   == " + rulesPendingModel);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
	}

	private void setCheckerRulesChildVO(List<BasePAModel> baseModelList, ProductPendingModel pendingModel) {
		RuleBean ruleBean = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
		try {

			Long ruleId = pendingModel.getRuleId();

			if (ruleId == null || ruleId == 0) {
				try {
					/* rulePendingModel.setIsCopyParentPK(true); */
					ruleId = this.ruleService.generateNewRuleId();
				} catch (Exception e) {
					// handle exception
				}
			}

			List<RulesPendingModel> rulePending = ruleService.getRuleFromPending(pendingModel.getPendingId(),
					this.getModuleId());
			if (log.isDebugEnabled()) {
				log.debug(">>>>>>>>>>>>rulePending=" + (rulePending == null ? 0 : rulePending.size()));
			}
			for (RulesPendingModel rulesPendingModel : rulePending) {
				RulesPendingModel tempModel = new RulesPendingModel();
				BeanUtils.copyProperties(rulesPendingModel, tempModel);
				tempModel.setRuleId(ruleId);
				tempModel.setCheckerId(super.getCurrentUserId());
				tempModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				tempModel.setIsCopyParentPK(Boolean.FALSE);
				baseModelList.add(tempModel);
				if (log.isDebugEnabled()) {
					log.debug("tempModel   == " + tempModel);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("baseModelList.size()    == " + baseModelList.size());
			}
		} catch (Exception e) {
			// handle this properly.
			log.error("Failed to retrieve pending rule");
		}

	}

	private List<String> compareChargeList(List<String> defaultList, List<String> selectedList) {
		List<String> tempList = new ArrayList<String>();

		Collection<String> s2 = CollectionUtils.subtract(defaultList, selectedList);
		for (String v : s2) {
			tempList.add(v);
		}
		return tempList;
	}

	private List<String> intersectionChargeList(List<String> defaultList, List<String> selectedList) {
		List<String> tempList = new ArrayList<String>();

		Collection<String> s2 = CollectionUtils.intersection(defaultList, selectedList);
		for (String v : s2) {
			tempList.add(v);
		}
		return tempList;
	}

	private void setProductChargeChildVO(String action, ProductDefinitionBean formBean, List<BasePAModel> baseModelList,
			ProductPendingModel parentProduct, boolean inLovNotChange) {
		List<String> removeList = null;
		List<String> addList = null;
		List<String> sameList = null;
		// if not from pending
		// if(formBean.getPendingId()==0){
		// US-GER Product Group Sanjeevi Start
		List<String> avbDefChargeList = new ArrayList<String>();
		List<AvailableCharrgeOnBean> defSelectedChargeList = formBean.getDefSelectedChargeOnList();
		if (defSelectedChargeList == null) {
			defSelectedChargeList = new ArrayList<AvailableCharrgeOnBean>();
		} else {

			for (AvailableCharrgeOnBean bean : defSelectedChargeList) {
				avbDefChargeList.add(bean.getAttributeId().toString());
			}
		}
		if (action.equals(ClientAction.DELETE.value())) {

			setChargeDetail(action, avbDefChargeList, BaseConstants.ActionType.DELETE.value(), baseModelList, formBean,
					parentProduct);
		} else {
			List<String> avbList = new ArrayList<String>();
			List<AvailableCharrgeOnBean> avbBeans = formBean.getSelectedChargeOnList();
			for (AvailableCharrgeOnBean bean : avbBeans) {
				avbList.add(bean.getAttributeId().toString());
			}

			removeList = compareChargeList(avbDefChargeList, avbList);
			addList = compareChargeList(avbList, avbDefChargeList);
			sameList = intersectionChargeList(avbList, avbDefChargeList);

			// US-GER Product Group Sanjeevi End
			// }else{

			// }
			for (String string : removeList) {
				if (log.isDebugEnabled()) {
					log.debug("remove list" + string);
				}
			}
			for (String string : addList) {
				if (log.isDebugEnabled()) {
					log.debug("addList list" + string);
				}
			}
			for (String string : sameList) {
				if (log.isDebugEnabled()) {
					log.debug("sameList list" + string);
				}
			}

			if (removeList != null && removeList.size() > 0) {
				// if(formBean.getPendingId()==0){
				// setChargeDetail(removeList,
				// BaseConstants.ActionType.DELETE.value(),
				// baseModelList,formBean);
				// }else{
				setChargeDetail(action, removeList, BaseConstants.ActionType.DISCARD.value(), baseModelList, formBean,
						parentProduct);
				// }
			}
			if (addList != null && addList.size() > 0) {
				setChargeDetail(action, addList, BaseConstants.ActionType.NEW.value(), baseModelList, formBean,
						parentProduct);
			}
			// if from master then insert into it
			// else no nid bother
			if (sameList != null && sameList.size() > 0) {
				if (formBean.getPendingId() == null || formBean.getPendingId() == 0) {
					String actionType = BaseConstants.ActionType.UNCHANGE.value();
					if (!inLovNotChange && formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())) {
						actionType = BaseConstants.ActionType.NEW.value();
					}
					setChargeDetail(action, sameList, actionType, baseModelList, formBean, parentProduct);
				} else {
					if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())) {
						setChargeDetail(action, sameList, BaseConstants.ActionType.UNCHANGE.value(), baseModelList,
								formBean, parentProduct);
					}
				}
			}
		}
		if (log.isDebugEnabled()) {
			if (log.isDebugEnabled()) {
				log.debug("baseModelList.size()    == " + baseModelList.size());
			}
		}
	}

	private void setChargeDetail(String action, List<String> chargeList, String actionType,
			List<BasePAModel> baseModelList, ProductDefinitionBean formBean, ProductPendingModel parentProduct) {
		ProductChargeOnPendingModel model = null;
		HashMap<String, ProductChargeOnModel> chargeMap = formBean.getDefaultChargeMap();
		HashMap<String, ProductChargeOnPendingModel> pendingCharge = null;
		if (chargeMap == null) {
			chargeMap = new HashMap<String, ProductChargeOnModel>();
		}
		for (String chargeOnId : chargeList) {
			model = new ProductChargeOnPendingModel();

			if (log.isDebugEnabled()) {
				log.debug("actionType    start== " + actionType);
				log.debug("chargeOnId    start== " + chargeOnId);
			}

			model.setChargeOnAttributeId(Long.parseLong(chargeOnId));
			if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())
					|| actionType.equals(BaseConstants.ActionType.UNCHANGE.value())) {
				if (formBean.getPendingId() > 0) {
					model.setActionType(null);
				} else {
					model.setActionType(actionType);
				}
			} else {
				model.setActionType(actionType);
			}
			model.setMakerId(super.getCurrentUserId());
			model.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
			model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
			model.setCountryCode(parentProduct.getCountryCode());
			model.setInterfaceName(parentProduct.getInterfaceName());
			model.setProductCode(parentProduct.getProductCode());

			if (actionType.equals(BaseConstants.ActionType.DISCARD.value())
					|| actionType.equals(BaseConstants.ActionType.UNCHANGE.value())) {
				pendingCharge = formBean.getPendingChargeMap();
				if (pendingCharge == null) {
					pendingCharge = new HashMap<String, ProductChargeOnPendingModel>();
				}
				if (formBean.getPendingId() > 0) {
					// check from pending table
					if (pendingCharge != null && pendingCharge.containsKey(chargeOnId + model.getProductCode())) {
						if (log.isDebugEnabled()) {
							log.debug("pendingMap.get(Long.parseLong(id)).getStatus()   = "
									+ pendingCharge.get(chargeOnId + model.getProductCode()).getStatus());
						}
						model.setStatus(pendingCharge.get(chargeOnId + model.getProductCode()).getStatus());
						model.setBaseVersion(pendingCharge.get(chargeOnId + model.getProductCode()).getBaseVersion());
						model.setCurrentVersion(
								pendingCharge.get(chargeOnId + model.getProductCode()).getCurrentVersion());
						model.setPendingId(pendingCharge.get(chargeOnId + model.getProductCode()).getPendingId());
						model.setParentPendingId(
								pendingCharge.get(chargeOnId + model.getProductCode()).getParentPendingId());

						if (actionType.equals(BaseConstants.ActionType.DISCARD.value())) {
							model.setActionType(BaseConstants.ActionType.DISCARD.value());
						}
					}
				} else if (chargeMap.containsKey(chargeOnId)
						&& actionType.equals(BaseConstants.ActionType.DISCARD.value())) {
					// check from master table
					ProductChargeOnModel charge = new ProductChargeOnModel();
					charge = chargeMap.get(chargeOnId);
					model.setBaseVersion(charge.getCurrentVersion());
					model.setStatus(charge.getStatus());
					model.setActionType(BaseConstants.ActionType.DELETE.value());
				}
			} else if (actionType.equals(BaseConstants.ActionType.NEW.value())) {
				model.setStatus(BaseConstants.Status.UNSAVE.value());
				model.setActionType(BaseConstants.ActionType.NEW.value());
			}

			baseModelList.add(model);

			if (log.isDebugEnabled()) {
				log.debug("formBean.getCountryCode()   == " + formBean.getCountryCode());
				log.debug("formBean.getInterfaceName()    == " + formBean.getInterfaceName());
				log.debug("formBean.getProductCode()    == " + formBean.getProductCode());
				log.debug("actionType    == " + actionType);
				log.debug("model.getStatus()    == " + model.getStatus());
				log.debug("model.getActionType()    == " + model.getActionType());
				log.debug("model.getProductCode()    == " + model.getProductCode());
				log.debug("model.getInterfaceName()    == " + model.getInterfaceName());
				log.debug("model.getBaseVersion()    == " + model.getBaseVersion());
				log.debug("model.getPendingId()    == " + model.getPendingId());
				log.debug("model.getParentPendingId()    == " + model.getParentPendingId());
			}
		}
	}

	private void setProductLOVChildVO(String action, ProductDefinitionBean formBean, List<BasePAModel> baseModelList,
			ProductPendingModel parentPending) {
		// save record from master table
		List<String> removeList = null;
		List<String> addList = null;
		List<String> sameList = null;
		// if(formBean.getPendingId()==0){
		// List<LovAttributeBean> defSelectedLovList =
		// formBean.getDefSelectedLovAttribute();
		List<String> defSelectedLovList = formBean.getDefSelectedLOVAttributesList();
		if (defSelectedLovList == null) {
			defSelectedLovList = new ArrayList<String>();
		}
		if (action.equals(ClientAction.DELETE.value())) {
			setLovDetail(action, defSelectedLovList, formBean.getSelectedLOVAttributesList(),
					BaseConstants.ActionType.DELETE.value(), baseModelList, formBean, parentPending);
		} else {

			removeList = compareChargeList(defSelectedLovList, formBean.getSelectedLOVAttributesList());
			addList = compareChargeList(formBean.getSelectedLOVAttributesList(), defSelectedLovList);
			sameList = intersectionChargeList(formBean.getSelectedLOVAttributesList(), defSelectedLovList);
			// }else{
			if (log.isDebugEnabled()) {
				log.debug("removeList    == " + removeList.size());
				log.debug("addList    == " + addList.size());
				log.debug("sameList    == " + sameList.size());
				// }
			}

			for (String string : removeList) {
				if (log.isDebugEnabled()) {
					log.debug("remove list" + string);
				}
			}
			for (String string : addList) {
				if (log.isDebugEnabled()) {
					log.debug("addList list" + string);
				}
			}
			for (String string : sameList) {
				if (log.isDebugEnabled()) {
					log.debug("sameList list" + string);
				}
			}
//US-GER Product Group Sanjeevi added "&& removeList.size()>0" in if condition
			if (removeList != null && removeList.size() > 0) {
				// if(formBean.getPendingId()==0){
				setLovDetail(action, removeList, formBean.getSelectedLOVAttributesList(),
						BaseConstants.ActionType.DISCARD.value(), baseModelList, formBean, parentPending);
				// }else{
				// setLovDetail(removeList,formBean.getSelectedLOVAttributesList(),
				// BaseConstants.ActionType.DELETE.value(), baseModelList);
				// }
			}
			// US-GER Product Group Sanjeevi added " && addList.size()>0" in if condition
			if (addList != null && addList.size() > 0) {
				setLovDetail(action, addList, formBean.getSelectedLOVAttributesList(),
						BaseConstants.ActionType.NEW.value(), baseModelList, formBean, parentPending);
			}
			// US-GER Product Group Sanjeevi added "&& sameList.size()>0" in if condition
			if (sameList != null && sameList.size() > 0) {
				if (formBean.getPendingId() == null || formBean.getPendingId() == 0) {
					setLovDetail(action, sameList, formBean.getSelectedLOVAttributesList(),
							BaseConstants.ActionType.UNCHANGE.value(), baseModelList, formBean, parentPending);
				} else {
					if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())) {
						setLovDetail(action, sameList, formBean.getSelectedLOVAttributesList(),
								BaseConstants.ActionType.UNCHANGE.value(), baseModelList, formBean, parentPending);
					}
				}
			}
		}
	}

	private void setLovDetail(String action, List<String> lovList, List<String> selectedList, String actionType,
			List<BasePAModel> baseModelList, ProductDefinitionBean formBean, ProductPendingModel parentPending) {
		ProductLovPendingModel model = null;
		HashMap<String, ProductLovPendingModel> pendingMap = null;
		if (actionType.equals(BaseConstants.ActionType.DISCARD.value())) {
			for (String id : lovList) {
				model = new ProductLovPendingModel();
				HashMap<String, ProductLovModel> lovMap = formBean.getDefaultLovMap();

				if (lovMap == null) {
					lovMap = new HashMap<String, ProductLovModel>();
				}
				if (lovMap.containsKey(id)) {
					ProductLovModel lov = new ProductLovModel();
					lov = lovMap.get(id);
					BeanUtils.copyProperties(lov, model);
					// model.setBaseVersion(lov.getCurrentVersion());
					// model.setStatus(lov.getStatus());
					model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					model.setActionType(BaseConstants.ActionType.DELETE.value());
				} else {
					/*
					 * log.debug("formBean.getPendingId() == "+formBean.getPendingId());
					 * log.debug("id == "+id);
					 * log.debug("formBean.getPendingLovMap() == "+formBean.getPendingLovMap());
					 * log.debug("formBean.getPendingLovMap().size == "+formBean.getPendingLovMap().
					 * size());
					 */
					/*
					 * Iterator iterator = formBean.getPendingLovMap().keySet().iterator(); while
					 * (iterator.hasNext()) { Object key = iterator.next(); ProductLovPendingModel
					 * value = formBean.getPendingLovMap().get(key); log.debug("key ====" +key); if
					 * (value != null) { log.debug("value.getStatus() " + value.getStatus()); } }
					 */
					pendingMap = formBean.getPendingLovMap();
					if (log.isDebugEnabled()) {
						log.debug("pendingMap.containsKey(id) == " + pendingMap.containsKey(id));
						log.debug("pendingMap   = " + pendingMap);
						log.debug("formBean.getPendingLovMap().containsKey(Long.parseLong(id)) == "
								+ formBean.getPendingLovMap().containsKey(Long.parseLong(id)));
					}
					if (formBean.getPendingId() != null && formBean.getPendingId() > 0) {
						if (pendingMap != null && pendingMap.containsKey(id)) {
							if (log.isDebugEnabled()) {
								log.debug("pendingMap.get(Long.parseLong(id)).getStatus()   = "
										+ pendingMap.get(id).getStatus());
							}
							model.setStatus(pendingMap.get(id).getStatus());
							model.setBaseVersion(pendingMap.get(id).getBaseVersion());
							model.setCurrentVersion(pendingMap.get(id).getCurrentVersion());
							model.setPendingId(pendingMap.get(id).getPendingId());
						}
					}
				}
				model.setActionType(actionType);
				model.setLovAttributeId(Long.parseLong(id));
				model.setMakerId(super.getCurrentUserId());
				model.setCountryCode(parentPending.getCountryCode());
				model.setInterfaceName(parentPending.getInterfaceName());
				model.setProductCode(parentPending.getProductCode());
				model.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				if (log.isDebugEnabled()) {
					log.debug("=========start===========");
					log.debug("id    == " + id);
					log.debug("formBean.getCountryCode()   == " + formBean.getCountryCode());
					log.debug("formBean.getInterfaceName()    == " + formBean.getInterfaceName());
					log.debug("formBean.getProductCode()    == " + formBean.getProductCode());
					log.debug("actionType    == " + actionType);
					log.debug("model.getStatus()    == " + model.getStatus());
					log.debug("model.getActionType()    == " + model.getActionType());
					log.debug("model.getProductCode()    == " + model.getProductCode());
					log.debug("model.getInterfaceName()    == " + model.getInterfaceName());
					log.debug("model.getBaseVersion()    == " + model.getBaseVersion());
					log.debug("model.getPendingId()    == " + model.getPendingId());
					log.debug("=========end===========");
				}
				baseModelList.add(model);
			}
		} else {
			for (String id : lovList) {
				model = new ProductLovPendingModel();
				/*
				 * if(actionType.equals(BaseConstants.ActionType.UNCHANGE.value() ) &&
				 * formBean.getPendingId()>0){ if(formBean.getPendingLovMap()!=null &&
				 * formBean.getPendingLovMap().containsKey(Long.parseLong(id))){ model
				 * .setStatus(formBean.getPendingLovMap().get(Long.parseLong (id)).getStatus());
				 * } }
				 */
				if (actionType.equals(BaseConstants.ActionType.UNCHANGE.value())
						&& (formBean.getPendingId() != null && formBean.getPendingId() > 0)) {
					pendingMap = formBean.getPendingLovMap();
					if (log.isDebugEnabled()) {
						log.debug("pendingMap.containsKey(id) == " + pendingMap.containsKey(id));
						log.debug("pendingMap   = " + pendingMap);
						log.debug("formBean.getPendingLovMap().containsKey(Long.parseLong(id)) == "
								+ formBean.getPendingLovMap().containsKey(Long.parseLong(id)));
					}
					if (pendingMap != null && pendingMap.containsKey(id)) {
						if (log.isDebugEnabled()) {
							log.debug("pendingMap.get(Long.parseLong(id)).getStatus()   = "
									+ pendingMap.get(id).getStatus());
						}
						model.setStatus(pendingMap.get(id).getStatus());
						model.setBaseVersion(pendingMap.get(id).getBaseVersion());
						model.setCurrentVersion(pendingMap.get(id).getCurrentVersion());
						model.setPendingId(pendingMap.get(id).getPendingId());
					}
				}

				model.setActionType(actionType);
				model.setLovAttributeId(Long.parseLong(id));
				model.setMakerId(super.getCurrentUserId());
				model.setCountryCode(parentPending.getCountryCode());
				model.setInterfaceName(parentPending.getInterfaceName());
				model.setProductCode(parentPending.getProductCode());
				model.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				long level = 1;
				if (log.isDebugEnabled()) {
					log.debug("=========start===========");
				}
				int i = 1;
				if (log.isDebugEnabled()) {
					log.debug("id    == " + id);
				}
				for (String attribute : selectedList) {
					if (log.isDebugEnabled()) {
						log.debug("attribute    == " + attribute);
					}
					if (attribute.equals(id)) {
						level = new Long(i);
						break;
					}
					i++;
				}

				if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())
						|| actionType.equals(BaseConstants.ActionType.UNCHANGE.value())) {
					if (formBean.getPendingId() != null && formBean.getPendingId() > 0) {
						model.setActionType(null);
					} else {
						model.setActionType(actionType);
					}
				} else {
					model.setActionType(actionType);
				}

				if (log.isDebugEnabled()) {

					log.debug("level    == " + level);
					log.debug("formBean.getCountryCode()   == " + formBean.getCountryCode());
					log.debug("formBean.getInterfaceName()    == " + formBean.getInterfaceName());
					log.debug("formBean.getProductCode()    == " + formBean.getProductCode());
					log.debug("actionType    == " + actionType);
					log.debug("model.getStatus()    == " + model.getStatus());
					log.debug("model.getActionType()    == " + model.getActionType());
					log.debug("model.getProductCode()    == " + model.getProductCode());
					log.debug("model.getInterfaceName()    == " + model.getInterfaceName());
					log.debug("model.getBaseVersion()    == " + model.getBaseVersion());
					log.debug("model.getPendingId()    == " + model.getPendingId());
					log.debug("=========end===========");
				}
				model.setLovLevel(level);
				level++;
				baseModelList.add(model);
			}
		}
	}

	private void setPendingSubProductChildVO(List<BasePAModel> baseModelList,
			List<ProductPendingModel> subProductPending) {
		for (ProductPendingModel productPendingModel : subProductPending) {
			baseModelList.add(productPendingModel);
		}
		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
	}

	// delete sub product content when it come from master table
	private void deleteSubProductChildVO(List<BasePAModel> parentBaseModelList, ProductDefinitionBean formBean,
			ProductPendingModel parentPending, ProductChargeBean chargeBean) throws Exception {
		ProductChargeActionBean chargeActionBean = (ProductChargeActionBean) super.getManagedBean(
				ProductChargeActionBean.BACKING_BEAN_NAME);
		List<ProductModel> productList = productDefinitionService.getSubProductList(parentPending.getCountryCode(),
				parentPending.getInterfaceName(), parentPending.getProductCode(), false);
		ProductPendingModel subProductPending = null;
		CheckerMakerVO makerVo = null;
		List<BasePAModel> subProdModelList = null;
		if (productList != null) {
			for (ProductModel product : productList) {
				makerVo = new CheckerMakerVO();
				subProdModelList = new ArrayList<BasePAModel>();

				subProductPending = new ProductPendingModel();
				BeanUtils.copyProperties(product, subProductPending);
				subProductPending.setMakerId(super.getCurrentUserId());
				subProductPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				subProductPending.setActionType(BaseConstants.ActionType.DELETE.value());
				subProductPending.setClientTimeZone(formBean.getClientTimeZone());
				makerVo.setParent(subProductPending);

				// delete charge code :)
				// chargeActionBean.setLovChargeCodesToBeSaved(subProductPending,subProdModelList);
				List<ChargeCodeModel> chargeCodeList = productDefinitionService.getChangeCodeList(
						subProductPending.getCountryCode(), subProductPending.getInterfaceName(),
						subProductPending.getProductCode());
				if (chargeCodeList != null) {
					ChargeCodePendingModel codePending = null;
					for (ChargeCodeModel chargeCodeModel : chargeCodeList) {
						codePending = new ChargeCodePendingModel();
						BeanUtils.copyProperties(chargeCodeModel, codePending);
						codePending.setBaseVersion(chargeCodeModel.getCurrentVersion());
						codePending.setActionType(BaseConstants.ActionType.DELETE.value());
						codePending.setMakerId(super.getCurrentUserId());
						codePending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(codePending);
					}
				}

				// delete chargemethod list get from charge method table
				List<ChargeMethodModel> chargeMethodList = productDefinitionService.getChargeMethodList(
						subProductPending.getCountryCode(), subProductPending.getInterfaceName(),
						subProductPending.getProductCode(), "");
				if (chargeMethodList != null) {
					ChargeMethodPendingModel methodPending = null;
					for (ChargeMethodModel chargeMethodModel : chargeMethodList) {
						methodPending = new ChargeMethodPendingModel();
						BeanUtils.copyProperties(chargeMethodModel, methodPending);
						methodPending.setBaseVersion(chargeMethodModel.getCurrentVersion());
						methodPending.setActionType(BaseConstants.ActionType.DELETE.value());
						methodPending.setMakerId(super.getCurrentUserId());
						methodPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(methodPending);
					}
				}

				List<ProductPLAccountModel> pandlList = productDefinitionService.getProductPLAccountList(
						subProductPending.getCountryCode(), subProductPending.getProductCode());

				if (pandlList != null) {
					ProductPLAccountPendingModel pandlPending = null;
					for (ProductPLAccountModel pandlModel : pandlList) {
						pandlPending = new ProductPLAccountPendingModel();
						BeanUtils.copyProperties(pandlModel, pandlPending);
						pandlPending.setBaseVersion(pandlModel.getCurrentVersion());
						pandlPending.setActionType(BaseConstants.ActionType.DELETE.value());
						pandlPending.setMakerId(super.getCurrentUserId());
						pandlPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(pandlPending);
					}
				}

				// setProductChargeChildVO(formBean,subProdModelList);
				List<ProductChargeOnModel> chargeOnList = productDefinitionService.getProductChangeOnList(
						subProductPending.getCountryCode(), subProductPending.getInterfaceName(),
						subProductPending.getProductCode());
				if (chargeOnList != null) {
					ProductChargeOnPendingModel chargePending = null;
					for (ProductChargeOnModel productChargeOnModel : chargeOnList) {
						chargePending = new ProductChargeOnPendingModel();
						BeanUtils.copyProperties(productChargeOnModel, chargePending);
						chargePending.setBaseVersion(productChargeOnModel.getCurrentVersion());
						chargePending.setActionType(BaseConstants.ActionType.DELETE.value());
						chargePending.setMakerId(super.getCurrentUserId());
						chargePending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(chargePending);
					}
				}

				try {
					List<RulesPendingModel> ruleList = ruleService.getRuleFromMaster(subProductPending.getRuleId());
					if (ruleList != null) {
						for (RulesPendingModel rulesPendingModel : ruleList) {
							rulesPendingModel.setActionType(BaseConstants.ActionType.DELETE.value());
							rulesPendingModel.setMakerId(super.getCurrentUserId());
							rulesPendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
							rulesPendingModel.setRuleFrom(this.getModuleId());
							subProdModelList.add(rulesPendingModel);
						}
					}
				} catch (Exception e) {
					// handle exception
				}
				makerVo.setChildList(subProdModelList);
				parentBaseModelList.add(makerVo);
			}
		}

	}

	// delete sub product content when it come from pending table
	private void deletePendingSubProductChildVO(List<BasePAModel> parentBaseModelList, ProductDefinitionBean formBean,
			ProductPendingModel parentPending, boolean isLovChanged) throws Exception {
		// SubProductVO[] subProd = formBean.getDefaultLovProductsArray();
		// ProductPendingModel subProductPending = null;

		List<ProductPendingModel> pendingSubProductList = productDefinitionService
				.getProductPendingByParentId(formBean.getPendingId());
		CheckerMakerVO makerVo = null;
		List<BasePAModel> subProdModelList = null;
		if (pendingSubProductList != null)
			for (ProductPendingModel pendingModel : pendingSubProductList) {
				makerVo = new CheckerMakerVO();
				subProdModelList = new ArrayList<BasePAModel>();
				pendingModel.setMakerId(super.getCurrentUserId());
				pendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				// if(pendingModel.getStatus()!=BaseConstants.Status.DELETE_DRAFT.value()){
				pendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
				// }
				pendingModel.setClientTimeZone(formBean.getClientTimeZone());
				makerVo.setParent(pendingModel);
				// delete charge code :)
				List<ChargeCodePendingModel> pendingCharge = productDefinitionService
						.getChargeCodePendingByParentId(pendingModel.getPendingId());
				for (ChargeCodePendingModel chargeCodePendingModel : pendingCharge) {
					chargeCodePendingModel.setMakerId(super.getCurrentUserId());
					chargeCodePendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					chargeCodePendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
					subProdModelList.add(chargeCodePendingModel);
				}

				// delete chargemethod list get from charge method table
				List<ChargeMethodPendingModel> pendingMethodList = productDefinitionService
						.getChargeMethodPendingByParentId(pendingModel.getPendingId());
				if (pendingMethodList != null) {
					for (ChargeMethodPendingModel chargeMethodPendingModel : pendingMethodList) {
						chargeMethodPendingModel.setMakerId(super.getCurrentUserId());
						chargeMethodPendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						chargeMethodPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
						subProdModelList.add(chargeMethodPendingModel);
					}
				}

				// setProductChargeChildVO(formBean,subProdModelList);
				List<ProductChargeOnPendingModel> pendingChargeOnList = productDefinitionService
						.getProductChargeByParentId(pendingModel.getPendingId());
				if (pendingChargeOnList != null) {
					for (ProductChargeOnPendingModel productChargeOnPendingModel : pendingChargeOnList) {
						productChargeOnPendingModel.setMakerId(super.getCurrentUserId());
						productChargeOnPendingModel
								.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						// if(productChargeOnPendingModel.getStatus()!=BaseConstants.Status.DELETE_DRAFT.value()){
						productChargeOnPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
						// }
						subProdModelList.add(productChargeOnPendingModel);
					}
				}
				// delete PLAccount list get from PLAccount table
				List<ProductPLAccountPendingModel> pendingPLAccountList = productDefinitionService
						.getProductPLAccountPendingByParentId(pendingModel.getPendingId());
				if (pendingPLAccountList != null) {
					for (ProductPLAccountPendingModel productPLAccountPendingModel : pendingPLAccountList) {
						productPLAccountPendingModel.setMakerId(super.getCurrentUserId());
						productPLAccountPendingModel
								.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						productPLAccountPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
						subProdModelList.add(productPLAccountPendingModel);
					}
				}

				/*
				 * List<ProductLovPendingModel> pendingLovList = productDefinitionService
				 * .getProductLovByParentId(pendingModel.getPendingId()); if
				 * (pendingLovList!=null) { for (ProductLovPendingModel productLovPendingModel :
				 * pendingLovList) {
				 * productLovPendingModel.setMakerId(super.getCurrentUserId());
				 * productLovPendingModel.setMakerDate(new java.sql.Timestamp(new
				 * java.util.Date().getTime())); productLovPendingModel
				 * .setActionType(BaseConstants.ActionType.DISCARD.value());
				 * subProdModelList.add(productLovPendingModel); } }
				 */

				// ok geh
				setRulesChildVO(subProdModelList, pendingModel, isLovChanged);
				makerVo.setChildList(subProdModelList);
				// pendingModel.setIsCopyParentPK(false);
				// log.debug(pendingModel);
				// parentBaseModelList.add(pendingModel);

				parentBaseModelList.add(makerVo);
			}
		// to set the vo list of deleted items
		// parentBaseModelList.add(makerVo);
	}

	private boolean setSubProductChildVO(String action, List<BasePAModel> parentBaseModelList,
			ProductDefinitionBean formBean, ProductPendingModel parentPending, ProductChargeBean chargeBean,
			ProductChargeActionBean chargeActionBean) throws Exception {

		boolean isLovNotChanged = true;
		if (log.isDebugEnabled()) {
			log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>setSubProductChildVO():action=" + action);
		}
		if (action.equals(ClientAction.DELETE.value())) {
			deleteSubProductChildVO(parentBaseModelList, formBean, parentPending, chargeBean);
		} else {
			isLovNotChanged = validateCartesianTableLov(formBean);
			if (!isLovNotChanged) {
				// if user search from pending table it discard the child
				// pending information
				if (formBean.getPendingId() != null && formBean.getPendingId() > 0) {
					deletePendingSubProductChildVO(parentBaseModelList, formBean, parentPending, isLovNotChanged);
				} else {
					deleteSubProductChildVO(parentBaseModelList, formBean, parentPending, chargeBean);
				}
			} else {
				// to validate whether the desc has been change
				if (!validateCartesianTableDesc(formBean)) {
					return false;
				}
			}

			SubProductVO[] subProd = formBean.getLovProductsArray();
			CheckerMakerVO makerVo = null;
			List<BasePAModel> subProdModelList = null;
			if (subProd != null) {
				if (log.isDebugEnabled()) {
					log.debug("subProd.length=" + subProd.length);
				}
				for (int i = 0; i < subProd.length; i++) {
					makerVo = new CheckerMakerVO();
					SubProductVO subProductVO = subProd[i];
					if (subProductVO != null) {
						ProductPendingModel subProductPending = new ProductPendingModel();
						BeanUtils.copyProperties(parentPending, subProductPending);

						setSubProductProperties(subProductVO, subProductPending, parentPending, formBean,
								isLovNotChanged);

						if (!StringUtils.hasLength(subProductPending.getDescription())) {
							FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED, new Object[] { FacesUtil
									.getMessageByKey(MessageConstants.PRODUCTDEFINITION_SUBPRODUCT_DESCRIPTION) });
							return false;
						}
						if (log.isDebugEnabled()) {
							log.debug("subProductVO=" + subProductVO);
							log.debug("formBean.getPendingId() = " + subProductPending.getPendingId());
							log.debug("formBean.getParentPendingId() = " + subProductPending.getParentPendingId());
							log.debug("status   = " + subProductPending.getStatus());
							log.debug("getActionType   = " + subProductPending.getActionType());
							// log.debug("status = " + subProductPending.get());
						}
						subProductPending.setClientTimeZone(formBean.getClientTimeZone());
						makerVo.setParent(subProductPending);
						subProdModelList = new ArrayList<BasePAModel>();

						// to save the charge code for sub product
						chargeActionBean.setLovChargeCodesToBeSaved(subProductPending, subProdModelList,
								formBean.getProductLovList(), formBean.getProductCode());

						if (!setChargeMethodChildVO(subProductPending, action, subProdModelList,
								subProductVO.getSubProductCode(), formBean)) {
							return false;
						}

						setProductChargeChildVO(action, formBean, subProdModelList, subProductPending, isLovNotChanged);
						// setProductLOVChildVO(action,
						// formBean,subProdModelList, subProductPending);

						setRulesChildVO(subProdModelList, subProductPending, isLovNotChanged);
						if (subProdModelList != null && subProdModelList.size() > 0)
							makerVo.setChildList(subProdModelList);
					}
					parentBaseModelList.add(makerVo);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("subProdModelList.size()    == " + (subProdModelList == null ? 0 : subProdModelList.size()));
			}
		}
		return true;
	}

	private void setSubProductProperties(SubProductVO subProductVO, ProductPendingModel childPending,
			ProductPendingModel parentPending, ProductDefinitionBean formBean, boolean isLovNotChanged)
			throws Exception {
		childPending.setBaseProductFlag(false);
		childPending.setParentProductCode(parentPending.getProductCode());
		childPending.setChargeRebateFlag(parentPending.getChargeRebateFlag());
		if (log.isDebugEnabled()) {
			log.debug("subProductVO=" + subProductVO);
		}
		if (subProductVO != null) {
			childPending.setProductCode(subProductVO.getSubProductCode());
			childPending.setDescription(subProductVO.getSubProductDesc());
			childPending.setHierarchyValue(subProductVO.getHierarchyValue());
			childPending.setProductGroupLevel(new Long(subProductVO.getProductGroupLevel()));
		}

		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			childPending.setActionType(BaseConstants.ActionType.NEW.value());
			childPending.setStatus(BaseConstants.Status.UNSAVE.value());
			childPending.setRuleId(new Long(0));
		} else if (formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())) {
			if (log.isDebugEnabled()) {
				log.debug("subProductVO.isModify()=" + subProductVO.isModify());
				log.debug("isLovNotChanged=" + isLovNotChanged);
			}
			// to check whether the lov cartesian is regenerated
			if (isLovNotChanged) {
				// to check whether the sub prod desc is change from existing
				// master record
				// if (subProductVO.isModify()) {
				childPending.setActionType(BaseConstants.ActionType.CHANGE.value());
				// }else{// if(formBean.getPendingId()==0){
				// childPending.setActionType(BaseConstants.ActionType.UNCHANGE.value());
				// }
			} else {
				childPending.setRuleId(new Long(0));
				childPending.setActionType(BaseConstants.ActionType.NEW.value());
				childPending.setStatus(BaseConstants.Status.UNSAVE.value());
			}

			// to set the child rule id from
			ProductModelPK prodPK = new ProductModelPK();
			prodPK.setCountryCode(childPending.getCountryCode());
			// prodPK.setInterfaceName(childPending.getInterfaceName());
			prodPK.setProductCode(childPending.getProductCode());
			ProductModel productMaster = productDefinitionService.getProduct(prodPK);
			if (log.isDebugEnabled()) {
				log.debug("productMaster=" + productMaster);
			}
			if (productMaster != null) {
				if (productMaster.getRuleId() != null) {
					if (log.isDebugEnabled()) {
						log.debug("productMaster.getRuleId()=" + productMaster.getRuleId());
					}
					childPending.setRuleId(productMaster.getRuleId());
				} else {
					childPending.setRuleId(new Long(0));
				}
				if (log.isDebugEnabled()) {
					log.debug("childPending.getRuleId()=" + childPending.getRuleId());
				}
			}

		}
		if (subProductVO.getPendingId() != null && subProductVO.getPendingId() > 0) {
			childPending.setPendingId(subProductVO.getPendingId());
		}
		if (parentPending.getPendingId() != null && parentPending.getPendingId() > 0) {
			childPending.setParentPendingId(parentPending.getPendingId());
		}
	}

	/**
	 * Approves the pending record.
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doApprove() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start");
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (!formBean.hasPendingId()) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED,
					new Object[] { FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID) });
			return null;
		}
		if (!formBean.validate()) {
			return null;
		}
		String keyAttributeLabel = formBean.getKeyAttributeLabel();
		if (this.processCheckerAction(ClientAction.APPROVE.value(), keyAttributeLabel, formBean.getPendingId(),
				formBean.getRejectComment())) {
			FacesUtil.addInfoMessage(null, MessageConstants.MSG_APPROVE_SUCCESS, new Object[] { keyAttributeLabel });
			formBean.resetSelectItems();
			// formBean.populateSearchInterfaceSelect();
			formBean.reset();
			// formBean.populateInterfaceSelect(formBean.getCountryCode());
			// Rule Setup
			RuleBean ruleForm = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
			/* ruleForm.resetRuleButton(); */
			ruleForm.resetRuleDetails();
			ruleForm.setListValues();
			if (log.isDebugEnabled()) {
				log.debug("end: formBean = [" + (ruleForm != null ? ruleForm.toString() : null) + "]");
				log.debug("end: formBean = " + formBean);
			}
			// Product charge setup?
			ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(
					ProductChargeBean.BACKING_BEAN_NAME);
			chargeBean.resetChargeCodeDetails();
			chargeBean.setChargeCodeList(null);
			chargeBean.setChargeCodeDeletedList(null);
			// Added by Srikanth For 4.4.1 multi language support -start
			chargeBean.setSubChargeInvList(null);
			chargeBean.setDeleteChargeInvList(null);
			chargeBean.clearMethodDetails();
			// ended
			// partner sharing
			chargeBean.setPartnerShareList(null);
			chargeBean.setDeletePartnerList(null);
			chargeBean.clearPartnerMethodDetails();
			chargeBean.setIsPartnerSharingEnabled(Boolean.FALSE);
			StaticDataUtil.partnerCahcheMap.clear();

			MethodBean methodForm = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
			methodForm.resetMethodDetails();
			methodForm.setSubMethodList(null);
			methodForm.setDeleteMethodList(null);
			/* methodForm.resetMethodButton(); */

			PandLSetupBean pandlForm = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
			pandlForm.resetPLAccountDetails();
			pandlForm.setSubPLAccountList(null);
			pandlForm.setDeletePLAccountList(null);
			StaticDataUtil.prodPLAccountCacheMap.clear();

			// Added by Srikanth For 4.4.1 multi language support -start
			ProductDefinitionBean otherInvDesForm = (ProductDefinitionBean) super.getManagedBean(
					ProductDefinitionBean.BACKING_BEAN_NAME);
			otherInvDesForm.doClearOtheInvDes();
			otherInvDesForm.setSubMethodList(null);
			otherInvDesForm.setDeleteMethodList(null);
			//// Added by Srikanth For 4.4.1 multi language support -ended
			StaticDataCacheLoader.getInstance().clearCatalog(ProductModel.class);
			StaticDataCacheLoader.getInstance().clearCatalog(RulesModel.class);
			StaticDataCacheLoader.getInstance().clearCatalog(ChargeCodeModel.class);
			StaticDataCacheLoader.getInstance().clearCatalog(ChargeMethodModel.class);
			StaticDataCacheLoader.getInstance().clearCatalog(ProductPLAccountModel.class);
		}
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = " + formBean);
		}
		return null;
	}

	/**
	 * Rejects the pending record.
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doReject() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start");
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (!formBean.hasPendingId()) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED,
					new Object[] { FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID) });
			return null;
		}
		if (!formBean.validate()) {
			return null;
		}
		String keyAttributeLabel = formBean.getKeyAttributeLabel();
		if (this.processCheckerAction(ClientAction.REJECT.value(), keyAttributeLabel, formBean.getPendingId(),
				formBean.getRejectComment())) {
			FacesUtil.addInfoMessage(null, MessageConstants.MSG_REJECT_SUCCESS, new Object[] { keyAttributeLabel });
			formBean.resetSelectItems();
			// formBean.populateSearchInterfaceSelect();
			formBean.reset();
			// formBean.populateInterfaceSelect(formBean.getCountryCode());
			// Rule Setup
			RuleBean ruleForm = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
			/* ruleForm.resetRuleButton(); */
			ruleForm.resetRuleDetails();
			ruleForm.setListValues();
			if (log.isDebugEnabled()) {
				log.debug("end: formBean = [" + (ruleForm != null ? ruleForm.toString() : null) + "]");
				log.debug("end: formBean = " + formBean);
			}
			// Product charge setup?
			ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(
					ProductChargeBean.BACKING_BEAN_NAME);
			chargeBean.resetChargeCodeDetails();
			chargeBean.setChargeCodeList(null);
			chargeBean.setChargeCodeDeletedList(null);
			// Added by Srikanth For 4.4.1 multi language support -start
			chargeBean.setSubChargeInvList(null);
			chargeBean.setDeleteChargeInvList(null);
			chargeBean.clearMethodDetails();
			// Added by Srikanth For 4.4.1 multi language support -ended

			// partner sharing
			chargeBean.setPartnerShareList(null);
			chargeBean.setDeletePartnerList(null);
			chargeBean.clearPartnerMethodDetails();
			chargeBean.setIsPartnerSharingEnabled(Boolean.FALSE);
			StaticDataUtil.partnerCahcheMap.clear();

			MethodBean methodForm = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
			methodForm.resetMethodDetails();
			methodForm.setSubMethodList(null);
			methodForm.setDeleteMethodList(null);
			/* methodForm.resetMethodButton(); */

			PandLSetupBean pandlForm = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
			pandlForm.resetPLAccountDetails();
			pandlForm.setSubPLAccountList(null);
			pandlForm.setDeletePLAccountList(null);
			StaticDataUtil.prodPLAccountCacheMap.clear();

			// Added by Srikanth For 4.4.1 multi language support -start
			ProductDefinitionBean otherInvDesForm = (ProductDefinitionBean) super.getManagedBean(
					ProductDefinitionBean.BACKING_BEAN_NAME);
			otherInvDesForm.doClearOtheInvDes();
			otherInvDesForm.setSubMethodList(null);
			otherInvDesForm.setDeleteMethodList(null);
			// Added by Srikanth For 4.4.1 multi language support -ended
		}
		if (log.isDebugEnabled()) {
			log.debug("end:  formBean = " + formBean);
		}
		return null;
	}

	/**
	 * Saves the master record with 'active' status, and then deletes all drafted
	 * records.
	 * 
	 * @param action the action
	 * @throws Exception if any error occurs
	 */
	private boolean processCheckerAction(final String action, final String keyAttributeLabel, final Long pendingId,
			final String rejectComment) throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("start: action = " + action + ", keyAttributeLabel = " + keyAttributeLabel + ", pendingId = "
					+ pendingId + ", rejectComment = " + rejectComment);
		}
		// validate client Action
		if (!action.equals(ClientAction.APPROVE.value()) && !action.equals(ClientAction.REJECT.value())) {
			throw new Exception(
					"[Product Definition] Invalid checker action = " + action + ", pendingId = " + pendingId);
		}
		// validate pending id
		if (pendingId <= 0) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED,
					new Object[] { FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID) });
			return false;
		}
		// to get the parent pending record with pending id
		ProductPendingModel parentPending = this.productDefinitionService
				.getProductByPendingId(formBean.getPendingId());
		if (parentPending == null) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_NOTFOUND, new Object[] {
					FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID), formBean.getPendingId() });
			return false;
		}
		if (action.equals(ClientAction.APPROVE.value()) && !validateRule(parentPending.getPendingId())) {
			FacesUtil.addErrorMessage(null, MessageConstants.CHARGEPARAM_RULE_ERROR_COMPILATION, new Object[] {
					FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID), formBean.getPendingId() });
			return false;
		}

		CheckerMakerVO checkerMakerVO = new CheckerMakerVO();
		List<DaSortOrderModel> daSortOrderList = new ArrayList<DaSortOrderModel>();
		daSortOrderList = productSortOrderService.getSortOrderByCountry(parentPending.getCountryCode());
		if (!generateProcessCheckerVO(action, parentPending, checkerMakerVO, daSortOrderList)) {
			return false;
		}

		// start-karthik@20120312 If product description is modified which has been set
		// in
		// DA_SORT_ORDER, update DA_SORT_ORDER.NODE_DESC, so that new changes is shown
		// in DA Fee screen
		if (daSortOrderList != null && daSortOrderList.size() > 0) {
			for (DaSortOrderModel oModel : daSortOrderList) {
				if (oModel.getNodeType().equals(DealSheetConstants.ProductSortNodeType.PRODUCT.value())
						&& oModel.getCountryCode().equals(parentPending.getCountryCode())
						&& oModel.getNodeCode().equals(parentPending.getProductCode())
						&& !(oModel.getNodeDesc().equals(parentPending.getDescription()))) {
					oModel.setNodeDesc(parentPending.getDescription());
					productSortOrderService.updateSortOrderDesc(oModel);
				}
			}
		}
		// end

		final BaseConstants.Status originalStatus = BaseConstants.Status.fromInt(parentPending.getStatus());
		if (log.isDebugEnabled()) {
			log.debug("originalStatus   =" + originalStatus);
		}
		if (action.equals(ClientAction.APPROVE.value())) {
			this.productDefinitionService.approve(checkerMakerVO, this.getModuleId());

			// populates form with new values or clear form
			if (originalStatus == BaseConstants.Status.NEW_SUBMIT
					|| originalStatus == BaseConstants.Status.CHANGE_SUBMIT) {
				// populate the new values into form
				if (log.isDebugEnabled()) {
					log.debug("approve liao   =");
				}
				ProductModelPK prodPK = new ProductModelPK();
				prodPK.setCountryCode(formBean.getCountryCode());
				// prodPK.setInterfaceName(formBean.getInterfaceName());
				prodPK.setProductCode(formBean.getProductCode());
				ProductModel productModel = productDefinitionService.getProduct(prodPK);

				if (productModel != null) {
					// BeanUtils.copyProperties(productModel, formBean); remarked by ectan@20090612
					// - standardize the
					// code (hk sit defect [4])

					// start added by ectan@20090612 - standardize the code (hk sit defect [4])
					formBean.setBaseProductFlag(productModel.getBaseProductFlag());
					formBean.setBranchCode(productModel.getBranchCode());
					formBean.setChargeRebateFlag(productModel.getChargeRebateFlag());
					formBean.setCheckerDate(productModel.getCheckerDate());
					formBean.setCheckerId(productModel.getCheckerId());
					formBean.setClientTimeZone(productModel.getClientTimeZone());
					formBean.setCountryCode(productModel.getCountryCode());
					formBean.setCreatedDate(productModel.getCreatedDate());
					formBean.setDescription(productModel.getDescription());
					formBean.setHierarchyValue(productModel.getHierarchyValue());
					formBean.setInterfaceName(productModel.getInterfaceName());
					formBean.setInvDescLocalLang(productModel.getInvDescLocalLang());
					formBean.setInvoiceDescription(productModel.getInvoiceDescription());
					formBean.setLowestEntityLevel(productModel.getLowestEntityLevel());
					formBean.setLowestFrequency(productModel.getLowestFrequency());
					formBean.setMainProduct(productModel.getMainProduct());
					formBean.setMakerDate(productModel.getMakerDate());
					formBean.setMakerId(productModel.getMakerId());
					formBean.setParentProductCode(productModel.getParentProductCode());
					formBean.setPrecedence(productModel.getPrecedence());
					formBean.setProductCategory(productModel.getProductCategory());
					formBean.setProductCode(productModel.getProductCode());
					formBean.setProductLevel(productModel.getProductLevel());
					formBean.setProductType(productModel.getProductType());
					formBean.setRuleId(productModel.getRuleId());
					formBean.setStatus(productModel.getStatus());
					// Non Gpbs product changes for product screen By Ramadevi T
					formBean.setNonGpbsPrdType(productModel.getNonGpbsPrdType());
					formBean.setNonGpbsRemarks(productModel.getNonGpbsRemarks());
					formBean.setPenaltyType(productModel.getPenaltyCtg());

					// Added by Sudhakar J for Transaction Grouping
					formBean.setTxnProductGrouping(productModel.getTxnProductGrouping());

					if (formBean.getProductDerivePriority() != null
							&& StringUtils.hasLength(productModel.getProductDerivePriority().toString())) {
						formBean.setProductDerivePriority(productModel.getProductDerivePriority().toString());
						if (productModel.getProductLevel().equalsIgnoreCase(ProductLevel.TXN.value()))
							formBean.setDuplicateFlag(productDefinitionService.checkProductDerivePriorityIfAvailable(
									productModel.getCountryCode(), ProductLevel.TXN.value(),
									productModel.getInterfaceName(),
									Integer.parseInt(productModel.getProductDerivePriority().toString())));
					}
					if (productModel.getUnitCost() != null) {
						formBean.setUnitCost(productModel.getUnitCost().toPlainString());
					}

					formBean.setUpdatedDate(productModel.getUpdatedDate());
					formBean.setWbdwProductCode(productModel.getWbdwProductCode());
					formBean.setWbdwProductCodeDesc(productModel.getWbdwProductCodeDesc());
					// end added by ectan@20090612 - standardize the code (hk sit defect [4])
					// Start - DPS
					formBean.setDpsProductCode(productModel.getDpsProductCode());
					formBean.setDpsProductType(productModel.getDpsProductType());
					// end - DPS
					// Start - CR#16
					formBean.setShowChargeOnAsVolumeFlag(productModel.getIsShowChargeOnAsVol());
					formBean.setVolumeChargeOnAttribute(productModel.getVolChargeOnAttrId());
					// end - CR#16
					// Start - CR#VolProduct_Derivation_GroupBy
					formBean.setGroupByAttrId(productModel.getGroupByAttrId());
					formBean.setTxnNarrative1(productModel.getTxnNarrative1());
					// End - CR#VolProduct_Derivation_GroupBy
					// Start - CR#Volume Tiered Pricing
					formBean.setTxnRollUpFlag(productModel.getTxnRollUpFlag());
					// End - CR#Volume Tiered Pricing

					// Added for charge on pass through flag.
					formBean.setChargePassThroughFlag(productModel.getChargePassThroughFlag());
					formBean.setGlobalRebateFlag(productModel.getGlobalRebateFlag());
					// added by ectan@20100907 - ph3 - itemise debit
					formBean.setProdShortDescription(productModel.getShortDescription());
					// MY FPX - reversal change
					formBean.setTaxCreditFlag(productModel.getTaxCreditFlag());

					formBean.setOnlineFlag(productModel.getOnlineFlag());
					formBean.setScpayProduct(productModel.getScpayProduct());
					formBean.setIsEnabledOnlineBilling(productDefinitionService
							.getOnlineBillingFlag(productModel.getCountryCode(), productModel.getInterfaceName()));

					doRead();
					// defect 134
					// Cascade delete PSGL_MAPPING, BILLING_PRODUCT and TIERING.
					cascadeDelete(checkerMakerVO, originalStatus, formBean.getCountryCode());
				} else {
					FacesUtil.addErrorMessage(null, MessageConstants.MSG_APPROVE_FAILED,
							new Object[] { keyAttributeLabel });
					return false;
				}
			} else if (originalStatus == BaseConstants.Status.DELETE_SUBMIT) {
				this.doNew();

				// defect 134
				// Cascade delete PSGL_MAPPING, BILLING_PRODUCT and TIERING.
				cascadeDelete(checkerMakerVO, originalStatus, formBean.getCountryCode());
			}
		} else if (action.equals(ClientAction.REJECT.value())) {
			if (!StringUtils.hasLength(rejectComment)) {
				FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED,
						new Object[] { FacesUtil.getMessageByKey(MessageConstants.LABEL_REJECTCOMMENT) });
				return false;
			}
			this.productDefinitionService.reject(checkerMakerVO, this.getModuleId(), rejectComment);

		}
		if (log.isDebugEnabled()) {
			log.debug("end");
		}
		return true;
	}

	private boolean generateProcessCheckerVO(final String action, ProductPendingModel parentPending,
			CheckerMakerVO checkerMakerVO, List<DaSortOrderModel> daSortOrderList) throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);

		int status = parentPending.getStatus();
		if (log.isDebugEnabled()) {
			log.debug("status   = " + status);
		}
		parentPending.setCheckerId(super.getCurrentUserId());
		parentPending.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
		if (log.isDebugEnabled()) {
			log.debug("parentPending   = " + parentPending);
		}
		parentPending.setBaseProductFlag(true);
		parentPending.setDisplayKeyValue(formBean.getKeyAttributeLabel());
		parentPending.setModuleId(this.getModuleId());
		parentPending.setClientTimeZone(formBean.getClientTimeZone());
		// set parent
		checkerMakerVO.setParent(parentPending);
		List<BasePAModel> baseModelList = new ArrayList<BasePAModel>();

		List<ChargeCodePendingModel> pendingCharge = productDefinitionService
				.getChargeCodePendingByParentId(parentPending.getPendingId());
		for (ChargeCodePendingModel chargeCodePendingModel : pendingCharge) {
			if (log.isDebugEnabled()) {
				log.debug("chargeCodePendingModel   == " + chargeCodePendingModel);
			}
			chargeCodePendingModel.setCheckerId(super.getCurrentUserId());
			chargeCodePendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));

			// start-karthik@20120312 If charge description is modified which has been set
			// in
			// DA_SORT_ORDER, update DA_SORT_ORDER.NODE_DESC, so that new changes is shown
			// in DA Fee screen
			if (daSortOrderList != null && daSortOrderList.size() > 0) {
				for (DaSortOrderModel oModel : daSortOrderList) {
					if (oModel.getNodeType().equals(DealSheetConstants.ProductSortNodeType.CHARGE.value())
							&& oModel.getCountryCode().equals(parentPending.getCountryCode())
							&& oModel.getNodeCode().equals(chargeCodePendingModel.getChargeCode())
							&& !(oModel.getNodeDesc().equals(chargeCodePendingModel.getDescription()))) {
						oModel.setNodeDesc(chargeCodePendingModel.getDescription());
						productSortOrderService.updateSortOrderDesc(oModel);
					}
				}
			}
			// end

			baseModelList.add(chargeCodePendingModel);
		}

		// delete chargemethod list get from charge method table
		List<ChargeMethodPendingModel> pendingMethodList = productDefinitionService
				.getChargeMethodPendingByParentId(parentPending.getPendingId());
		if (pendingMethodList != null) {
			for (ChargeMethodPendingModel chargeMethodPendingModel : pendingMethodList) {
				if (log.isDebugEnabled()) {
					log.debug("chargeMethodPendingModel   == " + chargeMethodPendingModel);
				}
				chargeMethodPendingModel.setCheckerId(super.getCurrentUserId());
				chargeMethodPendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(chargeMethodPendingModel);
			}
		}

		// delete PLAccount list get from PLAccount table
		List<ProductPLAccountPendingModel> pendingPLAccountList = productDefinitionService
				.getProductPLAccountPendingByParentId(parentPending.getPendingId());
		if (pendingPLAccountList != null) {
			for (ProductPLAccountPendingModel productPLAccountPendingModel : pendingPLAccountList) {
				if (log.isDebugEnabled()) {
					log.debug("chargeMethodPendingModel   == " + productPLAccountPendingModel);
				}
				productPLAccountPendingModel.setCheckerId(super.getCurrentUserId());
				productPLAccountPendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(productPLAccountPendingModel);
			}
		}
		// setProductChargeChildVO(formBean,subProdModelList);
		List<ProductChargeOnPendingModel> pendingChargeOnList = productDefinitionService
				.getProductChargeByParentId(parentPending.getPendingId());
		if (pendingChargeOnList != null) {
			for (ProductChargeOnPendingModel productChargeOnPendingModel : pendingChargeOnList) {
				if (log.isDebugEnabled()) {
					log.debug("productChargeOnPendingModel   == " + productChargeOnPendingModel);
				}
				productChargeOnPendingModel.setCheckerId(super.getCurrentUserId());
				productChargeOnPendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(productChargeOnPendingModel);
			}
		}

		List<ProductLovPendingModel> pendingLovList = productDefinitionService
				.getProductLovByParentId(parentPending.getPendingId());
		if (pendingLovList != null) {
			for (ProductLovPendingModel productLovPendingModel : pendingLovList) {
				if (log.isDebugEnabled()) {
					log.debug("productLovPendingModel   == " + productLovPendingModel);
				}
				productLovPendingModel.setCheckerId(super.getCurrentUserId());
				productLovPendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(productLovPendingModel);
			}
		}

		// ok geh
		// setRulesChildVO(baseModelList,parentPending, true);
		setCheckerRulesChildVO(baseModelList, parentPending);
		checkerSubProductChildVO(baseModelList, formBean, parentPending);

		// Added by Srikanth For 4.4.1 multi language support -start
		List<ProductInvoiceOthLangDesPending> pendingOthLangList = productDefinitionService
				.getOtherInvDesPendingByParentId(parentPending.getPendingId());
		if (pendingLovList != null) {
			for (ProductInvoiceOthLangDesPending pendingOthLangInv : pendingOthLangList) {
				if (log.isDebugEnabled()) {
					log.debug("ProductInvoiceOthLangDesPending   == " + pendingOthLangInv);
				}
				pendingOthLangInv.setCheckerId(super.getCurrentUserId());
				pendingOthLangInv.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(pendingOthLangInv);
			}
		}

		List<ChargeCodeMulLangPending> chargeOthLangList = productDefinitionService
				.getChargeOtherInvDesPendingByParentId(parentPending.getPendingId());
		if (pendingLovList != null) {
			for (ChargeCodeMulLangPending chargePendingOthLangInv : chargeOthLangList) {
				if (log.isDebugEnabled()) {
					log.debug("ChargeCodeMulLangPending   == " + chargePendingOthLangInv);
				}
				chargePendingOthLangInv.setCheckerId(super.getCurrentUserId());
				chargePendingOthLangInv.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(chargePendingOthLangInv);
			}
		}
		// Added by Srikanth For 4.4.1 multi language support -ended

		// added for partner sharing
		List<PartnerSharingPendingModel> partnerList = productDefinitionService
				.getpartnerPendingByParentId(parentPending.getPendingId());
		if (pendingLovList != null) {
			for (PartnerSharingPendingModel partnerPending : partnerList) {
				partnerPending.setCheckerId(super.getCurrentUserId());
				partnerPending.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				baseModelList.add(partnerPending);
			}
		}

		// added for global Product Mapping
		List<GlobalGpbsProductPendingModel> globalList = productDefinitionService
				.getglobalgpbsProductListPendingByParentId(parentPending.getPendingId());
		if (globalList != null) {
			for (GlobalGpbsProductPendingModel globalPending : globalList) {
				BaseConstants.Status originalStatus = BaseConstants.Status.fromInt(globalPending.getStatus());
				if (originalStatus == BaseConstants.Status.NEW_SUBMIT) {
					globalPending.setGlobalGpbsProductId(productDefinitionService.generateGlobalGpbsProductId());
				}
				/*
				 * globalPending.setCheckerId(super.getCurrentUserId());
				 * globalPending.setCheckerDate(new java.sql.Timestamp(new
				 * java.util.Date().getTime()));
				 */
				baseModelList.add(globalPending);
			}
		}

		// set child list
		checkerMakerVO.setChildList(baseModelList);

		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
		return true;
	}

	private void checkerSubProductChildVO(List<BasePAModel> parentBaseModelList, ProductDefinitionBean formBean,
			ProductPendingModel parentPending) throws Exception {
		// SubProductVO[] subProd = formBean.getDefaultLovProductsArray();
		// ProductPendingModel subProductPending = null;

		List<ProductPendingModel> pendingSubProductList = productDefinitionService
				.getProductPendingByParentId(parentPending.getPendingId());
		CheckerMakerVO makerVo = null;
		List<BasePAModel> subProdModelList = null;

		if (pendingSubProductList != null)
			for (ProductPendingModel pendingModel : pendingSubProductList) {
				makerVo = new CheckerMakerVO();
				subProdModelList = new ArrayList<BasePAModel>();
				if (log.isDebugEnabled()) {
					log.debug("pendingModel.getProductCode()   == " + pendingModel.getProductCode());
					log.debug("pendingModel.getBaseVersion()    == " + pendingModel.getBaseVersion());
					log.debug("pendingModel.getStatus()   == " + pendingModel.getStatus());
					log.debug("pendingModel.getParentPendingId()    == " + pendingModel.getParentPendingId());
					log.debug("pendingModel.getPendingId()    == " + pendingModel.getPendingId());
				}
				pendingModel.setCheckerId(super.getCurrentUserId());
				pendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				pendingModel.setIsCopyParentPK(false);
				pendingModel.setClientTimeZone(formBean.getClientTimeZone());
				makerVo.setParent(pendingModel);

				// delete charge code :)
				List<ChargeCodePendingModel> pendingCharge = productDefinitionService
						.getChargeCodePendingByParentId(pendingModel.getPendingId());
				for (ChargeCodePendingModel chargeCodePendingModel : pendingCharge) {
					if (log.isDebugEnabled()) {
						log.debug("chargeCodePendingModel   == " + chargeCodePendingModel);
					}

					chargeCodePendingModel.setCheckerId(super.getCurrentUserId());
					chargeCodePendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					subProdModelList.add(chargeCodePendingModel);
				}

				// delete chargemethod list get from charge method table
				List<ChargeMethodPendingModel> pendingMethodList = productDefinitionService
						.getChargeMethodPendingByParentId(pendingModel.getPendingId());
				if (pendingMethodList != null) {
					for (ChargeMethodPendingModel chargeMethodPendingModel : pendingMethodList) {
						if (log.isDebugEnabled()) {
							log.debug("chargeMethodPendingModel   == " + chargeMethodPendingModel);
						}
						chargeMethodPendingModel.setCheckerId(super.getCurrentUserId());
						chargeMethodPendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(chargeMethodPendingModel);
					}
				}

				// delete pandl list get from P&L Setup table
				List<ProductPLAccountPendingModel> pandlPendingList = productDefinitionService
						.getProductPLAccountPendingByParentId(pendingModel.getPendingId());
				if (pandlPendingList != null) {
					for (ProductPLAccountPendingModel pandlPendingModel : pandlPendingList) {
						if (log.isDebugEnabled()) {
							log.debug("chargeMethodPendingModel   == " + pandlPendingModel);
						}
						pandlPendingModel.setCheckerId(super.getCurrentUserId());
						pandlPendingModel.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(pandlPendingModel);
					}
				}

				// setProductChargeChildVO(formBean,subProdModelList);
				List<ProductChargeOnPendingModel> pendingChargeOnList = productDefinitionService
						.getProductChargeByParentId(pendingModel.getPendingId());
				if (pendingChargeOnList != null) {
					for (ProductChargeOnPendingModel productChargeOnPendingModel : pendingChargeOnList) {
						log.debug("productChargeOnPendingModel   == " + productChargeOnPendingModel);
						productChargeOnPendingModel.setCheckerId(super.getCurrentUserId());
						productChargeOnPendingModel
								.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
						subProdModelList.add(productChargeOnPendingModel);
					}
				}

				/*
				 * List<ProductLovPendingModel> pendingLovList = productDefinitionService
				 * .getProductLovByParentId(pendingModel.getPendingId()); if
				 * (pendingLovList!=null) { for (ProductLovPendingModel productLovPendingModel :
				 * pendingLovList) { subProdModelList.add(productLovPendingModel); } }
				 */

				// ok geh
				/* setRulesChildVO(subProdModelList,pendingModel, true); */
				setCheckerRulesChildVO(subProdModelList, pendingModel);
				makerVo.setChildList(subProdModelList);
				parentBaseModelList.add(makerVo);
			}
		// to set the vo list of deleted items
	}

	/**
	 * Performs delete function for draft/pending record.
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doDiscard() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start");
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("formBean = " + formBean);
		}
		if (!formBean.hasPendingId()) {
			FacesUtil.addErrorMessage(null, MessageConstants.ERROR_REQUIRED,
					new Object[] { FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID) });
			return null;
		}
		String keyAttributeLabel = formBean.getKeyAttributeLabel();

		// remove by cheah 15042010
		// defect 75 - this is the defect raised in UAT MY rollout.
		// if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
		// FacesUtil.addErrorMessage(null, MessageConstants.MSG_DELETESUBMIT_DENIED,
		// new Object[] { keyAttributeLabel });
		// return null;
		// }
		if (!formBean.validateKeyAttribute()) {
			return null;
		}
		formBean.setMaintenanceStep(MaintenanceStep.Remove.value());
		try {
			ProductPendingModel parentPending = this.productDefinitionService
					.getProductByPendingId(formBean.getPendingId());
			if (parentPending == null) {
				FacesUtil.addErrorMessage(null, MessageConstants.ERROR_NOTFOUND, new Object[] {
						FacesUtil.getMessageByKey(MessageConstants.LABEL_PENDINGID), formBean.getPendingId() });
				return null;
			}
			parentPending.setClientTimeZone(formBean.getClientTimeZone());
			CheckerMakerVO makerVo = new CheckerMakerVO();
			List<BasePAModel> baseModelList = new ArrayList<BasePAModel>();
			makerVo.setParent(parentPending);

			List<ChargeCodePendingModel> pendingCharge = productDefinitionService
					.getChargeCodePendingByParentId(formBean.getPendingId());
			for (ChargeCodePendingModel chargeCodePendingModel : pendingCharge) {
				chargeCodePendingModel.setMakerId(super.getCurrentUserId());
				chargeCodePendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				chargeCodePendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
				baseModelList.add(chargeCodePendingModel);
			}

			// delete chargemethod list get from charge method table
			List<ChargeMethodPendingModel> pendingMethodList = productDefinitionService
					.getChargeMethodPendingByParentId(formBean.getPendingId());
			if (pendingMethodList != null) {
				for (ChargeMethodPendingModel chargeMethodPendingModel : pendingMethodList) {
					chargeMethodPendingModel.setMakerId(super.getCurrentUserId());
					chargeMethodPendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					chargeMethodPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
					baseModelList.add(chargeMethodPendingModel);
				}
			}

			// setProductChargeChildVO(formBean,subProdModelList);
			List<ProductChargeOnPendingModel> pendingChargeOnList = productDefinitionService
					.getProductChargeByParentId(formBean.getPendingId());
			if (pendingChargeOnList != null) {
				for (ProductChargeOnPendingModel productChargeOnPendingModel : pendingChargeOnList) {
					productChargeOnPendingModel.setMakerId(super.getCurrentUserId());
					productChargeOnPendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					productChargeOnPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
					baseModelList.add(productChargeOnPendingModel);
				}
			}

			List<ProductLovPendingModel> pendingLovList = productDefinitionService
					.getProductLovByParentId(formBean.getPendingId());
			if (pendingLovList != null) {
				for (ProductLovPendingModel productLovPendingModel : pendingLovList) {
					productLovPendingModel.setMakerId(super.getCurrentUserId());
					productLovPendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					productLovPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
					baseModelList.add(productLovPendingModel);
				}
			}
			// Added by srikanth for 4.4.1 multi Language support -start
			List<ProductInvoiceOthLangDesPending> pendingOthLangList = productDefinitionService
					.getOtherInvDesPendingByParentId(parentPending.getPendingId());
			if (pendingOthLangList != null) {
				for (ProductInvoiceOthLangDesPending pendingOthLangInv : pendingOthLangList) {
					if (log.isDebugEnabled()) {
						log.debug("ProductInvoiceOthLangDesPending   == " + pendingOthLangInv);
					}
					pendingOthLangInv.setCheckerId(super.getCurrentUserId());
					pendingOthLangInv.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(pendingOthLangInv);
				}
			}

			List<ChargeCodeMulLangPending> chargePendingOthLangList = productDefinitionService
					.getChargeOtherInvDesPendingByParentId(parentPending.getPendingId());
			if (chargePendingOthLangList != null) {
				for (ChargeCodeMulLangPending chargeOthLangInvPending : chargePendingOthLangList) {
					if (log.isDebugEnabled()) {
						log.debug("ChargeCodeMulLangPending   == " + chargeOthLangInvPending);
					}
					chargeOthLangInvPending.setCheckerId(super.getCurrentUserId());
					chargeOthLangInvPending.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(chargeOthLangInvPending);
				}
			}
			// Added by srikanth for 4.4.1 multi Language support -ended

			// added for partner sharing

			List<PartnerSharingPendingModel> partnerList = productDefinitionService
					.getpartnerPendingByParentId(parentPending.getPendingId());
			if (partnerList != null) {
				for (PartnerSharingPendingModel partnerSharingPending : partnerList) {

					partnerSharingPending.setCheckerId(super.getCurrentUserId());
					partnerSharingPending.setCheckerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(partnerSharingPending);
				}
			}

			// added for PL Account mapping
			List<ProductPLAccountPendingModel> pendingPLAccountList = productDefinitionService
					.getProductPLAccountPendingByParentId(formBean.getPendingId());
			if (pendingPLAccountList != null) {
				for (ProductPLAccountPendingModel productPLAccountPendingModel : pendingPLAccountList) {
					productPLAccountPendingModel.setMakerId(super.getCurrentUserId());
					productPLAccountPendingModel.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					productPLAccountPendingModel.setActionType(BaseConstants.ActionType.DISCARD.value());
					baseModelList.add(productPLAccountPendingModel);
				}
			}
			setCheckerRulesChildVO(baseModelList, parentPending);

			deletePendingSubProductChildVO(baseModelList, formBean, parentPending, false);
			makerVo.setChildList(baseModelList);
			this.productDefinitionService.discard(makerVo, this.getModuleId(), true, false, null);

			FacesUtil.addInfoMessage(null, MessageConstants.MSG_DELETEDRAFT_SUCCESS,
					new Object[] { keyAttributeLabel });
			formBean.reset();
		} catch (Exception e) {
			FacesUtil.addErrorMessage(null, MessageConstants.MSG_DELETEDRAFT_FAILED,
					new Object[] { keyAttributeLabel });
			throw e;
		}

		doNew();
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = " + formBean);
		}
		return null;
	}

	/**
	 * Performs delete function. For master record, submits the record for deletion
	 * approval; For pending record, deletes the record immediately.
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doDelete() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start");
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);

		if (!formBean.validateKeyAttribute()) {
			return null;
		}
		if (log.isDebugEnabled()) {
			log.debug("formBean = " + formBean);
		}
		String keyAttributeLabel = formBean.getKeyAttributeLabel();
		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			FacesUtil.addErrorMessage(null, MessageConstants.MSG_DELETESUBMIT_DENIED,
					new Object[] { keyAttributeLabel });
			return null;
		}
		formBean.setMaintenanceStep(MaintenanceStep.Remove.value());
		if (!formBean.hasPendingId() && this.processMakerAction(ClientAction.DELETE.value(), keyAttributeLabel)) {
			FacesUtil.addInfoMessage(null, MessageConstants.MSG_DELETESUBMIT_SUCCESS,
					new Object[] { keyAttributeLabel });
		}
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = " + formBean);
		}
		return null;
	}

	/**
	 * Retrieves the pending record based on pending ID.
	 * 
	 * @param pendingId the pending ID
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doReadPending(String pendingId) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start: pendingId = " + pendingId);
		}
		final String toViewId = "viewProductDefinition";
		if (!super.validatePendingId(pendingId)) {
			if (log.isDebugEnabled()) {
				log.debug("return = " + toViewId);
			}
			return toViewId;
		}
		this.doNew();
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		formBean.setPendingId(Long.parseLong(pendingId));
		if (!formBean.validatePendingId()) {
			if (log.isDebugEnabled()) {
				log.debug("return = " + toViewId);
			}
			return toViewId;
		}
		ProductPendingModel productPending = productDefinitionService.getProductByPendingId(Long.parseLong(pendingId));
		if (productPending == null) {
			throw new BusinessException(ExceptionErrorCodes.ERROR_CODE_CHECKERMAKER_PENDING_NOTFOUND,
					new Object[] { formBean.getPendingId() });
		}
		if (productPending != null) {
			formBean.resetSelectItems();
			// formBean.populateSearchInterfaceSelect();
			formBean.reset();
			// BeanUtils.copyProperties(productPending, formBean); //remarked by
			// ectan@20090612 - standardize the code
			// (hk sit defect [4])

			// start added by ectan@20090612 - standardize the code (hk sit defect [4])
			formBean.setBaseProductFlag(productPending.getBaseProductFlag());
			formBean.setBranchCode(productPending.getBranchCode());
			formBean.setChargeRebateFlag(productPending.getChargeRebateFlag());
			formBean.setCheckerDate(productPending.getCheckerDate());
			formBean.setCheckerId(productPending.getCheckerId());
			formBean.setClientTimeZone(productPending.getClientTimeZone());
			formBean.setCountryCode(productPending.getCountryCode());
			formBean.setCreatedDate(productPending.getCreatedDate());
			formBean.setDescription(productPending.getDescription());
			formBean.setHierarchyValue(productPending.getHierarchyValue());
			formBean.setInterfaceName(productPending.getInterfaceName());
			formBean.setInvDescLocalLang(productPending.getInvDescLocalLang());
			formBean.setInvoiceDescription(productPending.getInvoiceDescription());
			formBean.setLowestEntityLevel(productPending.getLowestEntityLevel());
			formBean.setLowestFrequency(productPending.getLowestFrequency());
			formBean.setMainProduct(productPending.getMainProduct());
			formBean.setMakerDate(productPending.getMakerDate());
			formBean.setMakerId(productPending.getMakerId());
			formBean.setParentProductCode(productPending.getParentProductCode());
			formBean.setPrecedence(productPending.getPrecedence());
			formBean.setProductCategory(productPending.getProductCategory());
			formBean.setProductCode(productPending.getProductCode());
			formBean.setProductLevel(productPending.getProductLevel());
			formBean.setProductType(productPending.getProductType());
			formBean.setRuleId(productPending.getRuleId());
			formBean.setStatus(productPending.getStatus());
			// non gpbs changes By Ramadevi T
			formBean.setNonGpbsPrdType(productPending.getNonGpbsPrdType());
			formBean.setNonGpbsRemarks(productPending.getNonGpbsRemarks());
			formBean.setPenaltyType(productPending.getPenaltyCtg());
			// Added by Sudhakar J for Transaction Grouping
			formBean.setTxnProductGrouping(productPending.getTxnProductGrouping());

			if (productPending.getProductDerivePriority() != null
					&& StringUtils.hasLength(productPending.getProductDerivePriority().toString())) {
				formBean.setProductDerivePriority(productPending.getProductDerivePriority().toString());
				if (productPending.getProductLevel().equalsIgnoreCase(ProductLevel.TXN.value()))
					formBean.setDuplicateFlag(productDefinitionService.checkProductDerivePriorityIfAvailable(
							productPending.getCountryCode(), ProductLevel.TXN.value(),
							productPending.getInterfaceName(),
							Integer.parseInt(productPending.getProductDerivePriority().toString())));
			}
			if (productPending.getUnitCost() != null) {
				formBean.setUnitCost(productPending.getUnitCost().toPlainString());
			}
			// for #cr112 by cheah
			if (productPending.getRepetitiveLimit() != null) {
				formBean.setRepetitiveLimit(productPending.getRepetitiveLimit().toPlainString());
			}

			formBean.setUpdatedDate(productPending.getUpdatedDate());
			formBean.setWbdwProductCode(productPending.getWbdwProductCode());
			formBean.setWbdwProductCodeDesc(productPending.getWbdwProductCodeDesc());
			formBean.setPendingId(productPending.getPendingId());
			formBean.setParentProductCode(productPending.getParentProductCode());
			// end added by ectan@20090612 - standardize the code (hk sit defect [4])
			// Start - DPS
			formBean.setDpsProductCode(productPending.getDpsProductCode());
			formBean.setDpsProductType(productPending.getDpsProductType());
			// end - DPS
			// Start - CR#16
			formBean.setShowChargeOnAsVolumeFlag(productPending.getIsShowChargeOnAsVol());
			formBean.setVolumeChargeOnAttribute(productPending.getVolChargeOnAttrId());
			// end - CR#16
			// Start - CR#VolProduct_Derivation_GroupBy
			formBean.setAttributeList();
			if (productPending.getGroupByAttrId() != null) {
				formBean.setGroupByAttrId(productPending.getGroupByAttrId());
				formBean.setGroupByAttrNm(formBean.getAttributeName(productPending.getGroupByAttrId().toString()));
			}
			formBean.setTxnNarrative1(productPending.getTxnNarrative1());
			// End - CR#VolProduct_Derivation_GroupBy
			// Start - CR#Volume Tiered Pricing
			formBean.setTxnRollUpFlag(productPending.getTxnRollUpFlag());
			// End - CR#Volume Tiered Pricing

			// Added for charge on pass through flag.
			formBean.setChargePassThroughFlag(productPending.getChargePassThroughFlag());

			formBean.setGlobalRebateFlag(productPending.getGlobalRebateFlag());
			// MY FPX - reversal change
			formBean.setTaxCreditFlag(productPending.getTaxCreditFlag());

			formBean.setSubmittedFlag(this.productDefinitionService.isProductInApprovalQueue(
					productPending.getCountryCode(), productPending.getInterfaceName(), productPending.getProductCode(),
					this.getModuleId(), productPending.getPendingId()));
			formBean.setMaintenanceStep(MaintenanceStep.Modify.value());
			formBean.setProdShortDescription(productPending.getShortDescription());

			if (productPending.getAfpCode() != null && StringUtils.hasLength(productPending.getAfpCode())) {
				AfpModel afp = AfpUtil.getAfpModel(productPending.getAfpCode());
				if (afp != null) {
					formBean.setAfpFullName(afp.getAfpFullName());
					formBean.setAfpCode(productPending.getAfpCode());
					formBean.setGlobalProductCode(productPending.getGlobalProductCode());
				} else {
					formBean.setAfpFullName(null);
					formBean.setAfpCode(null);
					formBean.setGlobalProductCode(null);
				}
			} else {
				formBean.setAfpCode(null);
				formBean.setAfpFullName(null);
				formBean.setGlobalProductCode(null);
			}
			formBean.setMaskAmtFlag(productPending.getMaskAmtFlag());
			formBean.setBackdatedPSGLFlag(productPending.getBackdatedPSGLFlag());
			formBean.setChargePassThroughFlag(productPending.getChargePassThroughFlag());
			formBean.setGlobalRebateFlag(productPending.getGlobalRebateFlag());
			// MY FPX - reversal change
			formBean.setTaxCreditFlag(productPending.getTaxCreditFlag());

			formBean.setOnlineFlag(productPending.getOnlineFlag());
			formBean.setScpayProduct(productPending.getScpayProduct());
			formBean.setIsEnabledOnlineBilling(productDefinitionService
					.getOnlineBillingFlag(productPending.getCountryCode(), productPending.getInterfaceName()));
			this.getPendingDetails(formBean, pendingId);
		}

		return toViewId;
	}

	public void getPendingDetails(ProductDefinitionBean formBean, String pendingId) throws Exception {
		if (formBean.getProductLevel().equals(BaseConstants.ProductLevel.CROSSBORDER.value())
				|| formBean.getProductLevel().equals(BaseConstants.ProductLevel.CROSSPRODUCT.value())) {
			if (log.isDebugEnabled()) {
				log.debug("%*(**%$*((*&%^&   =");
			}
			formBean.getProductLevel(formBean.getProductLevel());
		} else {
			// US-GER Product Group Sanjeevi Start added ",formBean.getProductType()" in
			// argument list
			formBean.populateChargeOnSelect(formBean.getCountryCode(), formBean.getInterfaceName(),
					formBean.getProductType());
		}
		if (formBean.getInterfaceName() != null && formBean.getInterfaceName().equals(EFBS_MANUAL_INTERFACE)) {
			formBean.setManualProduct(true);
		} else {
			formBean.setManualProduct(false);
		}
		formBean.populateLovSelect(formBean.getCountryCode(), formBean.getInterfaceName());

		// to get default value from master table
		List<ProductChargeOnModel> chargeList = (List<ProductChargeOnModel>) productDefinitionService
				.getProductChangeOnList(formBean.getCountryCode(), formBean.getInterfaceName(),
						formBean.getProductCode());
		formBean.setDefaultChargeMap(putChargeListFromSearch(chargeList, formBean));
		List<ProductLovModel> lovList = (List<ProductLovModel>) productDefinitionService
				.getLovOnList(formBean.getCountryCode(), formBean.getInterfaceName(), formBean.getProductCode());
		formBean.setDefaultLovMap(putLovListFromSearch(lovList, formBean));

		try {
			/*
			 * formBean.setDefaultLovProductList(
			 * productDefinitionService.getLovProductsListFromMaster(
			 * formBean.getCountryCode(), formBean.getInterfaceName(),
			 * formBean.getProductCode() ) );
			 */

			formBean.setDefaultLovProductList(
					productDefinitionService.getProductPendingByParentId(formBean.getPendingId()));
		} catch (Exception ex) {
			log.error(ex);
		}

		List<ProductLovPendingModel> pendingLovList = (List<ProductLovPendingModel>) productDefinitionService
				.getProductLovByParentId(formBean.getPendingId());
		if (pendingLovList != null) {
			// List<ProductLovModel> temp = new ArrayList<ProductLovModel>();
			putPendingProductLovList(pendingLovList, formBean);
			// to generate the cartesian table
			if (log.isDebugEnabled()) {
				// log.debug("temp.size() == "+temp.size());
			}
			// formBean.setProductLovList(temp);
			// generate cartesian table :)
			this.doGenerateCartesianTable();
		}

		// get list from pending table
		List<ProductChargeOnPendingModel> pendingChargeList = (List<ProductChargeOnPendingModel>) productDefinitionService
				.getProductChargeByParentId(formBean.getPendingId());

		if (pendingChargeList != null) {
			putPendingProductChargeList(pendingChargeList, formBean);
		}

		if (pendingChargeList.size() > 0) {
			formBean.setIsShowChargeOnAsVolumeFlagDisabled(true);
		}

		// put all the charge code tat include sub product
		// for pendingChargeMap
		if (formBean.getLovProductsArray() != null) {
			for (SubProductVO subProd : formBean.getLovProductsArray()) {
				List<ProductChargeOnPendingModel> pending = (List<ProductChargeOnPendingModel>) productDefinitionService
						.getProductChargeByParentId(subProd.getPendingId());
				if (pending != null) {
					if (pendingChargeList == null)
						pendingChargeList = new ArrayList<ProductChargeOnPendingModel>();
					for (ProductChargeOnPendingModel productChargeOnPendingModel : pending) {
						pendingChargeList.add(productChargeOnPendingModel);
					}
				}
			}
		}

		if (pendingChargeList != null) {
			putPendingChargeMap(pendingChargeList, formBean);
		}

		// formBean.setLovAttribute(compareLovList(formBean.getLovAttribute(),
		// formBean.getSelectedLovAttribute()));

		// to populate the rules
		getRules(productDefinitionService.getPendingRule(Long.parseLong(pendingId), this.getModuleId()));

		// to populate the table for charge code and charge method :)
		getPendingChargeCodeTable(formBean);
		getPendingChargeMethodDefintionTable(formBean);
		getPendingPLAccountDefintionTable(formBean);

		// Added by srikanth for 4.4.1 multi Language support -start
		List<ChargeCodeMulLangPending> chargeInvoiceLangDescList = productDefinitionService
				.getChargeOtherInvDesPendingByParentId(formBean.getPendingId());
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		List<ChargeCodeMulLangPending> pendingList = null;
		if (chargeInvoiceLangDescList != null && chargeInvoiceLangDescList.size() > 0) {
			chargeBean.setSubChargeInvList(new ArrayList<ChargeCodeMulLangPending>());
			for (ChargeCodeMulLangPending pendingVal : chargeInvoiceLangDescList) {
				pendingList = new ArrayList<ChargeCodeMulLangPending>();
				pendingList.add(pendingVal);
				if (StaticDataUtil.languageCahcheMap != null
						&& StaticDataUtil.languageCahcheMap.containsKey(pendingVal.getChargeCode())) {
					pendingList.addAll(StaticDataUtil.languageCahcheMap.get(pendingVal.getChargeCode()));
					StaticDataUtil.languageCahcheMap.put(pendingVal.getChargeCode(), pendingList);
				} else
					StaticDataUtil.languageCahcheMap.put(pendingVal.getChargeCode(), pendingList);
			}

		}

		List<ProductInvoiceOthLangDesPending> invoiceLangDescList = productDefinitionService
				.getOtherInvDesPendingByParentId(formBean.getPendingId());

		if (invoiceLangDescList != null && invoiceLangDescList.size() > 0) {
			formBean.setSubMethodList(invoiceLangDescList);
		}
		// Added by srikanth for 4.4.1 multi Language support -ended

		// added for partner sharing
		List<PartnerSharingPendingModel> partnerSharingList = productDefinitionService
				.getpartnerPendingByParentId(formBean.getPendingId());
		List<PartnerSharingPendingModel> partnerPendingList = null;
		if (partnerSharingList != null && partnerSharingList.size() > 0) {
			chargeBean.setPartnerShareList(new ArrayList<PartnerSharingPendingModel>());
			for (PartnerSharingPendingModel pendingVal : partnerSharingList) {
				if (pendingVal.getPartnerSharingSequence() != 1) {
					partnerPendingList = new ArrayList<PartnerSharingPendingModel>();
					partnerPendingList.add(pendingVal);
					if (StaticDataUtil.partnerCahcheMap != null
							&& StaticDataUtil.partnerCahcheMap.containsKey(pendingVal.getChargeCode())) {
						partnerPendingList.addAll(StaticDataUtil.partnerCahcheMap.get(pendingVal.getChargeCode()));
						StaticDataUtil.partnerCahcheMap.put(pendingVal.getChargeCode(), partnerPendingList);
					} else {
						StaticDataUtil.partnerCahcheMap.put(pendingVal.getChargeCode(), partnerPendingList);
					}
				}
			}

		}
		// Getting Global Product Mapping Pending details
		List<GlobalGpbsProductPendingModel> globalProdPendingList = productDefinitionService
				.getglobalgpbsProductListPendingByParentId(formBean.getPendingId());
		List<GlobalGpbsProductPendingModel> globalPendingList = null;
		if (globalProdPendingList != null && globalProdPendingList.size() > 0) {
			chargeBean.setGlobalGpbsProductList(globalProdPendingList);
			for (GlobalGpbsProductPendingModel pendingVal : globalProdPendingList) {
				globalPendingList = new ArrayList<GlobalGpbsProductPendingModel>();
				globalPendingList.add(pendingVal);
				if (StaticDataUtil.globalProdCahcheMap != null
						&& StaticDataUtil.globalProdCahcheMap.containsKey(pendingVal.getChargeCode())) {
					globalPendingList.addAll(StaticDataUtil.globalProdCahcheMap.get(pendingVal.getChargeCode()));
					StaticDataUtil.globalProdCahcheMap.put(pendingVal.getChargeCode(), globalPendingList);
				} else
					StaticDataUtil.globalProdCahcheMap.put(pendingVal.getChargeCode(), globalPendingList);
			}
		}

		// Getting Global Product Mapping Pending details
		List<ProductPLAccountPendingModel> productPLAccountpendingList = productDefinitionService
				.getProductPLAccountPendingByParentId(formBean.getPendingId());
		PandLSetupBean pandLSetupForm = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		pandLSetupForm.setInterfaceName(formBean.getInterfaceName());
		List<ProductPLAccountPendingModel> productPLPendingList = null;
		if (productPLAccountpendingList != null && productPLAccountpendingList.size() > 0) {
			pandLSetupForm.setSubPLAccountList(productPLAccountpendingList);
			for (ProductPLAccountPendingModel pendingVal : productPLAccountpendingList) {
				productPLPendingList = new ArrayList<ProductPLAccountPendingModel>();
				productPLPendingList.add(pendingVal);
				if (StaticDataUtil.prodPLAccountCacheMap != null
						&& StaticDataUtil.prodPLAccountCacheMap.containsKey(pendingVal.getChargeCode())) {
					productPLPendingList.addAll(StaticDataUtil.prodPLAccountCacheMap.get(pendingVal.getChargeCode()));
					StaticDataUtil.prodPLAccountCacheMap.put(pendingVal.getChargeCode(), productPLPendingList);
				} else
					StaticDataUtil.prodPLAccountCacheMap.put(pendingVal.getChargeCode(), productPLPendingList);
			}
		}

		formBean.setMaintenanceStep(MaintenanceStep.Modify.value());
		setPopulatePrimaryKeyFields();
	}

	/**
	 * Processes the clicked table row.
	 * 
	 * @param event the action event listener
	 */
	@Override
	public void doClickTableRow(SelectEvent<?> event) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("start: event.component.clientId = "
					+ event.getComponent().getClientId(FacesContext.getCurrentInstance()) + ", event.componentId = "
					+ event.getComponent().getId());
		}
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("start: formBean = " + formBean);
		}
		if (formBean.getSearchTable() != null && formBean.getSearchTable().getRowCount() > 0) {
			if (log.isDebugEnabled()) {
				log.debug("selectedRowIndex = " + formBean.getSearchTable().getRowIndex() + ", selectedRowData = "
						+ formBean.getSearchTable().getRowData());
			}
			ProductDefinitionVO selectedRow = (ProductDefinitionVO) event.getObject();
			if (selectedRow == null) {
				FacesUtil.addErrorMessage(null, MessageConstants.LABEL_TABLEROWUNSELECTED);
				return;
			}
			formBean.reset();
			// BeanUtils.copyProperties(selectedRow, formBean);
			ProductModelPK prodPK = new ProductModelPK();
			prodPK.setCountryCode(selectedRow.getCountryCode());
			// prodPK.setInterfaceName(selectedRow.getInterfaceName());
			prodPK.setProductCode(selectedRow.getProductCode());
			ProductModel productMaster = productDefinitionService.getProduct(prodPK);

			if (productMaster != null) {
				// BeanUtils.copyProperties(productMaster, formBean); //remarked by
				// ectan@20090612 - standardized the
				// code (hk sit defect [4])

				// start added by ectan@20090612 - standardized the code (hk sit defect [4])
				formBean.setBaseProductFlag(productMaster.getBaseProductFlag());
				formBean.setBranchCode(productMaster.getBranchCode());
				formBean.setChargeRebateFlag(productMaster.getChargeRebateFlag());
				formBean.setCheckerDate(productMaster.getCheckerDate());
				formBean.setCheckerId(productMaster.getCheckerId());
				formBean.setClientTimeZone(productMaster.getClientTimeZone());
				formBean.setCountryCode(productMaster.getCountryCode());
				formBean.setCreatedDate(productMaster.getCreatedDate());
				formBean.setDescription(productMaster.getDescription());
				formBean.setHierarchyValue(productMaster.getHierarchyValue());
				formBean.setInterfaceName(productMaster.getInterfaceName());
				formBean.setInvDescLocalLang(productMaster.getInvDescLocalLang());
				formBean.setInvoiceDescription(productMaster.getInvoiceDescription());
				formBean.setLowestEntityLevel(productMaster.getLowestEntityLevel());
				formBean.setLowestFrequency(productMaster.getLowestFrequency());
				formBean.setMainProduct(productMaster.getMainProduct());
				formBean.setMakerDate(productMaster.getMakerDate());
				formBean.setMakerId(productMaster.getMakerId());
				formBean.setParentProductCode(productMaster.getParentProductCode());
				formBean.setPrecedence(productMaster.getPrecedence());
				formBean.setProductCategory(productMaster.getProductCategory());
				formBean.setProductCode(productMaster.getProductCode());
				formBean.setProductLevel(productMaster.getProductLevel());
				formBean.setProductType(productMaster.getProductType());
				formBean.setRuleId(productMaster.getRuleId());
				formBean.setStatus(productMaster.getStatus());
				formBean.setOnlineFlag(productMaster.getOnlineFlag());
				formBean.setScpayProduct(productMaster.getScpayProduct());
				formBean.setIsEnabledOnlineBilling(productDefinitionService
						.getOnlineBillingFlag(productMaster.getCountryCode(), productMaster.getInterfaceName()));
				// Non Gpbs changes by Ramadevi T
				formBean.setNonGpbsPrdType(productMaster.getNonGpbsPrdType());
				formBean.setNonGpbsRemarks(productMaster.getNonGpbsRemarks());
				formBean.setPenaltyType(productMaster.getPenaltyCtg());
				if (productMaster.getNonGpbsPrdType() != null) {

					formBean.setIsRemarksEnable(true);
				} else {
					formBean.setIsRemarksEnable(false);
				}
				if (formBean.getOnlineFlag() == Boolean.TRUE) {
					formBean.setEnablePLAccount(Boolean.FALSE);
				} else {
					formBean.setEnablePLAccount(Boolean.TRUE);
				}
				// Added by Sudhakar J for Transaction Grouping
				formBean.setTxnProductGrouping(productMaster.getTxnProductGrouping());
				if (productMaster.getChargePassThroughFlag() != null ? productMaster.getChargePassThroughFlag()
						: Boolean.FALSE) {
					formBean.setTxnProductGroupAllowed(Boolean.FALSE);
				} else if ((ProductLevel.TXN.value().equals(productMaster.getProductLevel())
						&& ProductType.CHARGE.value().equals(productMaster.getProductType()))
						&& !(InterfaceSystem.GPBS.value().equals(productMaster.getInterfaceName())
								|| InterfaceSystem.MANUAL.value().equals(productMaster.getInterfaceName()))) {
					formBean.setTxnProductGroupAllowed(Boolean.TRUE);
				} else {
					formBean.setTxnProductGroupAllowed(Boolean.FALSE);
				}

				if (productMaster.getProductDerivePriority() != null
						&& StringUtils.hasLength(productMaster.getProductDerivePriority().toString())) {
					formBean.setProductDerivePriority(productMaster.getProductDerivePriority().toString());
					if (productMaster.getProductLevel().equalsIgnoreCase(ProductLevel.TXN.value()))
						formBean.setDuplicateFlag(productDefinitionService.checkProductDerivePriorityIfAvailable(
								productMaster.getCountryCode(), ProductLevel.TXN.value(),
								productMaster.getInterfaceName(),
								Integer.parseInt(productMaster.getProductDerivePriority().toString())));
				}

				if (productMaster.getUnitCost() != null) {
					formBean.setUnitCost(productMaster.getUnitCost().toPlainString());
				}
				// for #cr112 by cheah
				if (productMaster.getRepetitiveLimit() != null) {
					formBean.setRepetitiveLimit(productMaster.getRepetitiveLimit().toPlainString());
				}

				formBean.setUpdatedDate(productMaster.getUpdatedDate());
				formBean.setWbdwProductCode(productMaster.getWbdwProductCode());
				formBean.setWbdwProductCodeDesc(productMaster.getWbdwProductCodeDesc());
				// end added by ectan@20090612 - standardized the code (hk sit defect [4])
				// Start - DPS
				formBean.setDpsProductCode(productMaster.getDpsProductCode());
				formBean.setDpsProductType(productMaster.getDpsProductType());
				// end - DPS
				// Start - CR#16
				formBean.setShowChargeOnAsVolumeFlag(productMaster.getIsShowChargeOnAsVol());
				formBean.setVolumeChargeOnAttribute(productMaster.getVolChargeOnAttrId());
				// end - CR#16
				// Start - CR#VolProduct_Derivation_GroupBy
				if (ProductLevel.VOL.value().equalsIgnoreCase(formBean.getProductLevel())) {
					formBean.setAttributeList();
				}
				// formBean.setGroupByAttrId(productMaster.getGroupByAttrId());
				if (productMaster.getGroupByAttrId() != null) {
					formBean.setGroupByAttrId(productMaster.getGroupByAttrId());
					formBean.setGroupByAttrNm(formBean.getAttributeName(productMaster.getGroupByAttrId().toString()));
				}
				formBean.setTxnNarrative1(productMaster.getTxnNarrative1());
				// End - CR#VolProduct_Derivation_GroupBy
				// Start - CR#Volume Tiered Pricing
				formBean.setTxnRollUpFlag(productMaster.getTxnRollUpFlag());
				// End - CR#Volume Tiered Pricing
				formBean.setProdShortDescription(productMaster.getShortDescription());

				// Added for charge on pass through flag.
				formBean.setChargePassThroughFlag(productMaster.getChargePassThroughFlag());

				formBean.setGlobalRebateFlag(productMaster.getGlobalRebateFlag());
				// MY FPX - reversal change
				formBean.setTaxCreditFlag(productMaster.getTaxCreditFlag());

				if (productMaster.getAfpCode() != null && StringUtils.hasLength(productMaster.getAfpCode())) {
					AfpModel afp = AfpUtil.getAfpModel(productMaster.getAfpCode());
					if (afp != null) {
						formBean.setAfpFullName(afp.getAfpFullName());
						formBean.setAfpCode(productMaster.getAfpCode());
						formBean.setGlobalProductCode(productMaster.getGlobalProductCode());
					} else {
						formBean.setAfpFullName(null);
						formBean.setAfpCode(null);
						formBean.setGlobalProductCode(null);
					}
				} else {
					formBean.setAfpCode(null);
					formBean.setAfpFullName(null);
					formBean.setGlobalProductCode(null);
				}
				formBean.setMaskAmtFlag(productMaster.getMaskAmtFlag());
				formBean.setBackdatedPSGLFlag(productMaster.getBackdatedPSGLFlag());

			}

			if (log.isDebugEnabled()) {
				log.debug("getStatus " + formBean.getStatus());
				log.debug("getMakerId " + formBean.getMakerId());
				log.debug("getCheckerId " + formBean.getCheckerId());
				log.debug("getCheckerDate " + formBean.getCheckerDate());
				log.debug("getMakerDate " + formBean.getMakerDate());
			}

			this.doRead();
		} else {
			if (log.isDebugEnabled()) {
				log.error("Search table is null");
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = " + formBean);
		}
	}

	/**
	 * Retrieves the master record based on primary key(s).
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doRead() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		formBean.setMaintenanceStep(MaintenanceStep.Modify.value());
		getMasterDetails(formBean);
		if (log.isDebugEnabled()) {
			log.debug("getStatus " + formBean.getStatus());
			log.debug("getMakerId " + formBean.getMakerId());
			log.debug("getCheckerId " + formBean.getCheckerId());
			log.debug("getCheckerDate " + formBean.getCheckerDate());
			log.debug("getMakerDate " + formBean.getMakerDate());
			log.debug("end:  formBean = " + formBean);
		}
		return null;
	}

	private void getMasterDetails(ProductDefinitionBean formBean) throws Exception {
		String countryCode = formBean.getCountryCode();
		String interfaceName = formBean.getInterfaceName();
		String productCode = formBean.getProductCode();

		if (interfaceName != null && interfaceName.equals(EFBS_MANUAL_INTERFACE)) {
			formBean.setManualProduct(true);
		} else {
			formBean.setManualProduct(false);
		}

		formBean.setSubmittedFlag(this.productDefinitionService.isProductInApprovalQueue(countryCode, interfaceName,
				productCode, this.getModuleId(), null));

		// reset the cartesian table
		formBean.setCartesianArray(new String[0][0]);
		formBean.setLovProductsArray(new SubProductVO[0]);

		// Rule Setup
		RuleBean ruleForm = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
		/* ruleForm.resetRuleButton(); */
		ruleForm.resetRuleDetails();
		ruleForm.setListValues();
		ruleForm.setSubRulesList(null);
		if (log.isDebugEnabled()) {
			log.debug("end: formBean = [" + (ruleForm != null ? ruleForm.toString() : null) + "]");
			log.debug("end: formBean = " + formBean);
		}
		// Product charge setup?
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		chargeBean.resetChargeCodeDetails();
		chargeBean.setChargeCodeList(null);
		chargeBean.setChargeCodeDeletedList(null);
		chargeBean.setGlobalGpbsProductList(null);
		List<GlobalGpbsProductModel> globalGpbsProductModelList = this.productDefinitionService
				.getGlobalGPBSProductId(countryCode, productCode);
		if (globalGpbsProductModelList != null) {
			List<GlobalGpbsProductPendingModel> globalGpbsProductPendingList = new ArrayList<GlobalGpbsProductPendingModel>();

			for (GlobalGpbsProductModel model : globalGpbsProductModelList) {
				GlobalGpbsProductPendingModel globalGpbsProductPendingModel = new GlobalGpbsProductPendingModel();
				BeanUtils.copyProperties(model, globalGpbsProductPendingModel);
				globalGpbsProductPendingModel.setBaseVersion(globalGpbsProductPendingModel.getCurrentVersion());
				globalGpbsProductPendingList.add(globalGpbsProductPendingModel);
			}
			chargeBean.setGlobalGpbsProductList(globalGpbsProductPendingList);
		}
		// Added by srikanth for 4.4.1 multi Language support -start
		chargeBean.doClearChargeOtheInvDes();
		chargeBean.setSubChargeInvList(null);
		chargeBean.setDeleteChargeInvList(null);
		// Added by srikanth for 4.4.1 multi Language support -end

		// added for partner sharing
		chargeBean.clearPartnerMethodDetails();
		chargeBean.setPartnerShareList(null);
		chargeBean.setDeletePartnerList(null);
		chargeBean.setIsPartnerSharingEnabled(Boolean.FALSE);
		StaticDataUtil.partnerCahcheMap.clear();

		MethodBean methodForm = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		methodForm.resetMethodDetails();
		methodForm.setSubMethodList(null);
		methodForm.setDeleteMethodList(null);

		// Product P AND L Account Read from DB - starts
		PandLSetupBean pandLSetupForm = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		pandLSetupForm.resetPLAccountDetails();
		pandLSetupForm.setSubPLAccountList(null);
		pandLSetupForm.setDeletePLAccountList(null);
		StaticDataUtil.prodPLAccountCacheMap.clear();
		List<ProductPLAccountModel> productPLAccountModelList = this.productDefinitionService
				.getProductPLAccountList(countryCode, productCode);

		List<ProductPLAccountPendingModel> productPLAccountPendingList = new ArrayList<ProductPLAccountPendingModel>();
		if (productPLAccountModelList != null) {
			for (ProductPLAccountModel model : productPLAccountModelList) {
				ProductPLAccountPendingModel productPLAccountPendingModel = new ProductPLAccountPendingModel();
				BeanUtils.copyProperties(model, productPLAccountPendingModel);
				productPLAccountPendingModel.setBaseVersion(productPLAccountPendingModel.getCurrentVersion());
				productPLAccountPendingModel.setPlAccount(model.getPlAccount());
				productPLAccountPendingList.add(productPLAccountPendingModel);
			}
			pandLSetupForm.setSubPLAccountList(productPLAccountPendingList);
		}
		List<ProductPLAccountPendingModel> pendingPLAccountList = null;
		if (productPLAccountPendingList != null) {
			// chargeBean.setSubChargeInvList(new ArrayList<ChargeCodeMulLangPending>());
			for (ProductPLAccountPendingModel pendingVal : productPLAccountPendingList) {
				pendingPLAccountList = new ArrayList<ProductPLAccountPendingModel>();
				pendingPLAccountList.add(pendingVal);
				if (StaticDataUtil.prodPLAccountCacheMap != null
						&& StaticDataUtil.prodPLAccountCacheMap.containsKey(pendingVal.getProductCode())) {
					pendingPLAccountList.addAll(StaticDataUtil.prodPLAccountCacheMap.get(pendingVal.getProductCode()));
					StaticDataUtil.prodPLAccountCacheMap.put(pendingVal.getProductCode(), pendingPLAccountList);
				} else
					StaticDataUtil.prodPLAccountCacheMap.put(pendingVal.getProductCode(), pendingPLAccountList);
			}

		}

		// Product P AND L Account Read from DB - Ends

		/* methodForm.resetMethodButton(); */
		// Added by srikanth for 4.4.1 multi Language support -start
		ProductDefinitionBean otherInvDescForm = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		otherInvDescForm.doClearOtheInvDes();
		otherInvDescForm.setSubMethodList(null);
		otherInvDescForm.setDeleteMethodList(null);
		// Added by srikanth for 4.4.1 multi Language support -end

		if (log.isDebugEnabled()) {
			log.debug("countryCode   =" + countryCode);
			log.debug("interfaceName   =" + interfaceName);
			log.debug("productCode   =" + productCode);
			log.debug("formBean.getProductType()   =" + formBean.getProductType());
			log.debug("formBean.getProductLevel()   =" + formBean.getProductLevel());
		}

		// to populate unselect lov and charge select items
		/*
		 * if(formBean.getProductLevel().equals(PRODUCT_LEVLE_CROSS_BORDER)){ formBean
		 * .setSelectedChargeOnAttributes(formBean.putChargeListBasedOnProdLevel ());
		 * }else{ formBean.populateChargeOnSelect(formBean.getCountryCode(),
		 * formBean.getInterfaceName()); }
		 */
		if (formBean.getProductLevel().equals(BaseConstants.ProductLevel.CROSSBORDER.value())
				|| formBean.getProductLevel().equals(BaseConstants.ProductLevel.CROSSPRODUCT.value())) {
			if (log.isDebugEnabled()) {
				log.debug("%*(**%$*((*&%^&   =");
			}
			formBean.getProductLevel(formBean.getProductLevel());
		} else {
			// US-GER Product Group Sanjeevi Start added ",formBean.getProductType()" in
			// argument list
			formBean.populateChargeOnSelect(formBean.getCountryCode(), formBean.getInterfaceName(),
					formBean.getProductType());
		}
		formBean.populateLovSelect(formBean.getCountryCode(), formBean.getInterfaceName());

		// GET CHARGE ON ATTRIBUTE
		List<ProductChargeOnModel> chargeList = (List<ProductChargeOnModel>) productDefinitionService
				.getProductChangeOnList(countryCode, interfaceName, productCode);

		if (chargeList != null) {
			putChargeListFromSearch(chargeList, formBean);
			if (chargeList.size() > 0) {
				formBean.setIsShowChargeOnAsVolumeFlagDisabled(true);
			}
		}

		try {
			formBean.setDefaultLovProductList(
					productDefinitionService.getLovProductsListFromMaster(countryCode, interfaceName, productCode));
		} catch (Exception ex) {
			log.error(ex);
		}

		List<ProductLovModel> lovList = (List<ProductLovModel>) productDefinitionService.getLovOnList(countryCode,
				interfaceName, productCode);
		if (lovList != null) {
			putLovListFromSearch(lovList, formBean);
			// to generate the cartesian table
			formBean.setProductLovList(lovList);

			this.doGenerateCartesianTable();
			// SubProductVO[] array = formBean.getLovProductsArray();
			// formBean.setDefaultLovProductsArray(array);
		}

		// to populate the rule tab
		doReadRule(formBean);

		// formBean.setLovAttribute(compareLovList(formBean.getLovAttribute(),
		// formBean.getSelectedLovAttribute()));

		// formBean.setLovProductsArray( lovProductsArray );

		// to get the charge code definition and charge method
		getChargeMethodDefintionTable(formBean);
		getChargeCodeTable(formBean);
		getPandLTable(formBean);

		setPopulatePrimaryKeyFields();

		// Added by srikanth for 4.4.1 multi Language support -start
		List<ProductInvoiceOthLangDesModel> productInvlist = productDefinitionService
				.getProdDefOtherLangInvDescMasterList(countryCode, interfaceName, productCode);
		ProductInvoiceOthLangDesPending prodinvPending = null;
		List<ProductInvoiceOthLangDesPending> prodinvPendingList = new ArrayList<ProductInvoiceOthLangDesPending>();

		if (productInvlist != null && productInvlist.size() > 0) {
			for (ProductInvoiceOthLangDesModel prodInvMaster : productInvlist) {
				prodinvPending = new ProductInvoiceOthLangDesPending();
				prodinvPending.setProductCode(prodInvMaster.getProductCode());
				prodinvPending.setInterfaceName(prodInvMaster.getInterfaceName());
				prodinvPending.setCountryCode(prodInvMaster.getCountryCode());
				prodinvPending.setLanguageCodeDesc(prodInvMaster.getLanguageCodeDesc());
				prodinvPending.setOtherLangInvDescription(prodInvMaster.getOtherLangInvDescription());
				prodinvPending.setLanguageIsoCode(prodInvMaster.getLanguageIsoCode());
				prodinvPending.setBaseVersion(prodInvMaster.getCurrentVersion());
				prodinvPendingList.add(prodinvPending);

			}
			formBean.setSubMethodList(prodinvPendingList);
		}

		// ChargeCode tab multiLangInvSupporting
		List<ChargeCodeMulLangModel> chargeInvlist = productDefinitionService
				.getChargeCodeOtherLangInvDescMasterList(countryCode, interfaceName, productCode);
		ChargeCodeMulLangPending chargeInvPending = null;
		List<ChargeCodeMulLangPending> chargeInvPendingList = new ArrayList<ChargeCodeMulLangPending>();
		if (chargeInvlist != null && chargeInvlist.size() > 0) {
			for (ChargeCodeMulLangModel prodInvMaster : chargeInvlist) {
				chargeInvPending = new ChargeCodeMulLangPending();
				chargeInvPending.setProductCode(prodInvMaster.getProductCode());
				chargeInvPending.setChargeCode(prodInvMaster.getChargeCode());
				chargeInvPending.setInterfaceName(prodInvMaster.getInterfaceName());
				chargeInvPending.setCountryCode(prodInvMaster.getCountryCode());
				chargeInvPending.setChargeLanguageCodeDesc(prodInvMaster.getChargeLanguageCodeDesc());
				chargeInvPending
						.setChargeCodeOtherLangInvDescription(prodInvMaster.getChargeCodeOtherLangInvDescription());
				chargeInvPending.setLanguageIsoCode(prodInvMaster.getLanguageIsoCode());
				chargeInvPendingList.add(chargeInvPending);

			}
			List<ChargeCodeMulLangPending> pendingList = null;
			if (chargeInvPendingList != null) {
				chargeBean.setSubChargeInvList(new ArrayList<ChargeCodeMulLangPending>());
				for (ChargeCodeMulLangPending pendingVal : chargeInvPendingList) {
					pendingList = new ArrayList<ChargeCodeMulLangPending>();
					pendingList.add(pendingVal);
					if (StaticDataUtil.languageCahcheMap != null
							&& StaticDataUtil.languageCahcheMap.containsKey(pendingVal.getChargeCode())) {
						pendingList.addAll(StaticDataUtil.languageCahcheMap.get(pendingVal.getChargeCode()));
						StaticDataUtil.languageCahcheMap.put(pendingVal.getChargeCode(), pendingList);
					} else
						StaticDataUtil.languageCahcheMap.put(pendingVal.getChargeCode(), pendingList);
				}

			}

		}
		// Added by srikanth for 4.4.1 multi Language support -ended
		EnableDisbaleSetupModel enableDisbaleSetupModel = productDefinitionService.getEnableDisableSetupModel(
				countryCode, BaseConstants.DEFAULT_BUSINESS_SEGMENT, BaseConstants.PARTNER_SHARING);
		if (enableDisbaleSetupModel != null && enableDisbaleSetupModel.getEnable_Disable_Flg() != null
				&& enableDisbaleSetupModel.getEnable_Disable_Flg().equalsIgnoreCase(BaseConstants.ENABLE_FLAG)) {
			chargeBean.setIsPartnerSharingEnabled(Boolean.TRUE);
		}
		// added for partner sharing
		List<ChargePartnerSharingModel> partnerModelList = productDefinitionService
				.getPartnerSharingByProductCode(countryCode, interfaceName, productCode);
		if (CollectionUtils.isNotEmpty(partnerModelList)) {
			List<PartnerSharingPendingModel> partnerPendingList = new ArrayList<PartnerSharingPendingModel>();
			PartnerSharingPendingModel partnerSharingPendingModel = null;
			for (ChargePartnerSharingModel chargePartnerSharingModel : partnerModelList) {
				if (chargePartnerSharingModel.getPartnerSharingSequence() != 1) {
					partnerSharingPendingModel = new PartnerSharingPendingModel();
					partnerSharingPendingModel.setProductCode(chargePartnerSharingModel.getProductCode());
					partnerSharingPendingModel.setChargeCode(chargePartnerSharingModel.getChargeCode());
					partnerSharingPendingModel
							.setPartnerSharingSuspense(chargePartnerSharingModel.getPartnerSharingSuspense());
					partnerSharingPendingModel
							.setPartnerSharingPercent(chargePartnerSharingModel.getPartnerSharingPercent());
					partnerSharingPendingModel
							.setPartnerSharingSequence(chargePartnerSharingModel.getPartnerSharingSequence());
					partnerSharingPendingModel.setPartnerSharingAccountCurrency(
							chargePartnerSharingModel.getPartnerSharingAccountCurrency());
					// partnerSharingPendingModel.setCurrentVersion(chargePartnerSharingModel.getCurrentVersion());
					partnerSharingPendingModel.setCountryCode(chargePartnerSharingModel.getCountryCode());

					partnerPendingList.add(partnerSharingPendingModel);
				}

			}
			// chargeBean.setPartnerShareList(partnerPendingList);
			List<PartnerSharingPendingModel> pendingList = null;
			if (partnerPendingList != null) {
				chargeBean.setPartnerShareList(new ArrayList<PartnerSharingPendingModel>());
				for (PartnerSharingPendingModel pendingVal : partnerPendingList) {
					pendingList = new ArrayList<PartnerSharingPendingModel>();
					pendingList.add(pendingVal);
					if (StaticDataUtil.partnerCahcheMap != null
							&& StaticDataUtil.partnerCahcheMap.containsKey(pendingVal.getChargeCode())) {
						pendingList.addAll(StaticDataUtil.partnerCahcheMap.get(pendingVal.getChargeCode()));
						StaticDataUtil.partnerCahcheMap.put(pendingVal.getChargeCode(), pendingList);
					} else {
						StaticDataUtil.partnerCahcheMap.put(pendingVal.getChargeCode(), pendingList);
					}
				}

			}
			productDefinitionService.updatePatrnerSharingCurrentVersion(countryCode, interfaceName, productCode);
		} else {
			chargeBean.setPartnerShareList(new ArrayList<PartnerSharingPendingModel>());
		}

	}

	public void doReadRule(ProductDefinitionBean formBean) {
		// To get the rules information.
		try {
			RuleBean ruleBean = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);

			// To set the values into Rules tab.
			ruleBean.setCountryCode(formBean.getCountryCode());
			ruleBean.setInterfaceName(formBean.getInterfaceName());
			ruleBean.setProductCode(formBean.getProductCode());
			ruleBean.setProductDesc(formBean.getDescription());
			ruleBean.setOnlineFlag(formBean.getOnlineFlag());
			ruleBean.setPaymentProductType(formBean.getScpayProduct());

			List<RulesPendingModel> rulesList = ruleService.getRuleFromMaster(formBean.getRuleId());
			List<RulesPendingModel> rulesList2 = new ArrayList<RulesPendingModel>();
			rulesList2.addAll(rulesList);

			ruleBean.setSubRulesList(rulesList2);
			ruleBean.setDefRulesList(rulesList2);
			if (rulesList != null) {
				if (log.isDebugEnabled()) {
					log.debug("rulesList - " + rulesList.toString());
				}

				if (rulesList.size() == 1 && rulesList.get(0).getRuleType() != null) {
					if (rulesList.get(0).getRuleType().equals(RuleCategory.EXPR.toString())) {
						ruleBean.setExpressionFlag(true);
						ruleBean.setFunctionFlag(false);
						ruleBean.setRuleType(RuleCategory.EXPR.toString());
					} else if (rulesList.get(0).getRuleType().equals(RuleCategory.FUNCT.toString())) {
						ruleBean.setFunctionFlag(true);
						ruleBean.setExpressionFlag(false);
						ruleBean.setRuleType(RuleCategory.FUNCT.toString());
					} else {
						ruleBean.setDisplaySubRulesList(rulesList);
						ruleBean.setExpressionFlag(false);
						ruleBean.setFunctionFlag(false);
						ruleBean.setRuleType(RuleCategory.BASIC.toString());
					}
				} else {
					ruleBean.setDisplaySubRulesList(rulesList);
					ruleBean.setExpressionFlag(false);
					ruleBean.setFunctionFlag(false);
					ruleBean.setRuleType(RuleCategory.BASIC.toString());
				}
			} else {
				ruleBean.setDisplaySubRulesList(null);
			}
			ruleBean.updateDerivationRule();

			if (formBean.getProductLevel() != null
					&& formBean.getProductLevel().equals(ProductLevel.CROSSBORDER.value())) {
				ruleBean.setIsCrossBorderCrossProduct(true);
				if (ruleBean.getExpressionFlag() || ruleBean.getFunctionFlag()) {
					ruleBean.setAttributeListForCrossProduct(INTERFACE_SYSTEM_EFBS);
				}
				/* ruleBean.setRuleCountryList(); */
			} else if (formBean.getProductLevel() != null
					&& formBean.getProductLevel().equals(ProductLevel.CROSSPRODUCT.value())) {
				ruleBean.setIsCrossProduct(true);
				if (ruleBean.getExpressionFlag() || ruleBean.getFunctionFlag()) {
					ruleBean.setAttributeListForCrossProduct(INTERFACE_SYSTEM_EFBS);
				}
				/* ruleBean.setRuleProductList( formBean.getCountryCode() ); */
			} else {
				ruleBean.setIsCrossProduct(false);
				ruleBean.setIsCrossBorderCrossProduct(false);
				ruleBean.setAttributeList();
			}

		} catch (Exception ex) {
			log.error("Fail to retrieve rule:" + formBean.getRuleId() + " with exception: " + ex);
		}
	}

	private void getPandLTable(ProductDefinitionBean formBean) throws Exception {
		PandLSetupBean pandlBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("getChar" + "geCodeTable -- start ");
		}
		List<ProductPLAccountPendingModel> chargePendingList = new ArrayList<ProductPLAccountPendingModel>();

		// get from main product
		List<ChargeCodeModel> allChargeList = productDefinitionService.getChangeCodeList(formBean.getCountryCode(),
				formBean.getInterfaceName(), formBean.getProductCode());
		/*
		 * putChargeCodePendingList(chargeList,
		 * chargePendingList,formBean.getProductCode());
		 */

		// get from sub product code

		// putChargeCodePendingList(allChargeList, chargePendingList,
		// formBean.getProductCode());

		// put it to charge method table List
		/*
		 * if (chargePendingList != null) {
		 * chargeBean.setChargeCodeList(chargePendingList);
		 * chargeBean.setDefChargeCodeList(chargePendingList); }
		 */

		pandlBean.setCountryCode(formBean.getCountryCode());
		pandlBean.setInterfaceName(formBean.getInterfaceName());
		pandlBean.setProductCodeList();
	}

	private void getChargeCodeTable(ProductDefinitionBean formBean) throws Exception {
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("getChar" + "geCodeTable -- start ");
		}
		List<ChargeCodePendingModel> chargePendingList = new ArrayList<ChargeCodePendingModel>();

		// get from main product
		List<ChargeCodeModel> allChargeList = productDefinitionService.getChangeCodeList(formBean.getCountryCode(),
				formBean.getInterfaceName(), formBean.getProductCode());
		/*
		 * putChargeCodePendingList(chargeList,
		 * chargePendingList,formBean.getProductCode());
		 */

		// get from sub product code
		if (formBean.getLovProductsArray() != null) {
			for (int i = 0; i < formBean.getLovProductsArray().length; i++) {
				SubProductVO prodVC = (SubProductVO) formBean.getLovProductsArray()[i];
				if (log.isDebugEnabled()) {
					log.debug("prodVC.getSubProductCode()  == " + prodVC.getSubProductCode());
				}
				if (prodVC.getSubProductCode() != null) {
					List<ChargeCodeModel> chargeList = new ArrayList<ChargeCodeModel>();
					chargeList = productDefinitionService.getChangeCodeList(formBean.getCountryCode(),
							formBean.getInterfaceName(), prodVC.getSubProductCode());
					allChargeList.addAll(chargeList);
					/*
					 * putChargeCodePendingList(chargeList,
					 * chargePendingList,formBean.getProductCode());
					 */
				}
			}
		}
		putChargeCodePendingList(allChargeList, chargePendingList, formBean.getProductCode());

		// put it to charge method table List
		if (chargePendingList != null) {
			chargeBean.setChargeCodeList(chargePendingList);
			chargeBean.setDefChargeCodeList(chargePendingList);
		}

		chargeBean.setCountryCode(formBean.getCountryCode());
		chargeBean.setInterfaceName(formBean.getInterfaceName());
		chargeBean.setProductCodeList();
	}

	private void putChargeCodePendingList(List<ChargeCodeModel> chargeList,
			List<ChargeCodePendingModel> chargePendingList, String baseProductCode) throws Exception {
		List<ChargeCodePendingModel> lovAttributeList = new ArrayList<ChargeCodePendingModel>();
		List<ChargeCodePendingModel> cloneList = new ArrayList<ChargeCodePendingModel>();
		List<ChargeCodePendingModel> tempList = new ArrayList<ChargeCodePendingModel>();

		ChargeCodePendingModel pendingModel = null;

		ProductChargeBean prodChargeBean = (ProductChargeBean) super.getManagedBean(
				ProductChargeBean.BACKING_BEAN_NAME);

		if (chargeList != null) {
			if (log.isDebugEnabled()) {
				log.debug("baseProductCode=" + baseProductCode);
			}
			for (ChargeCodeModel model : chargeList) {
				pendingModel = new ChargeCodePendingModel();
				BeanUtils.copyProperties(model, pendingModel);
				pendingModel.setBaseVersion(model.getCurrentVersion());
				if (log.isDebugEnabled()) {
					log.debug("pendingModel.getProductCode()=" + pendingModel.getProductCode());
				}
				if (pendingModel.getLovAttribute() != null || pendingModel.getLovId() != null
						|| !BaseConstants.ZERO.equals(pendingModel.getLovAttribute())
						|| !BaseConstants.ZERO.equals(pendingModel.getLovId())) {
					if (lovAttributeList == null || lovAttributeList.size() == 0) {
						lovAttributeList.add(pendingModel);
					} else {
						boolean isFound = false;
						for (ChargeCodePendingModel chargeCode : lovAttributeList) {
							if (chargeCode.getChargeCode().equalsIgnoreCase(pendingModel.getChargeCode())) {
								isFound = true;
								break;
							}

						}
						if (log.isDebugEnabled()) {
							log.debug("existing record with same lov exist?" + isFound);
						}
						if (!isFound) {
							lovAttributeList.add(pendingModel);
						}
					}

				}

				// DO not display the cloned charge codes from base product.
				else if (!baseProductCode.equals(pendingModel.getProductCode())) {
					tempList.add(pendingModel);
				} else if (baseProductCode.equals(pendingModel.getProductCode())) {
					cloneList.add(pendingModel);
					chargePendingList.add(pendingModel);
				} else {

					chargePendingList.add(pendingModel);
				}

			}

			// When display, if there is any LOV attribute tied to the charge
			// code,
			// display the record as base product with LOV attribute and values.
			if (log.isDebugEnabled()) {
				log.debug("size of lovAttributeList:" + (lovAttributeList == null ? 0 : lovAttributeList.size()));
			}
			for (ChargeCodePendingModel chargeCode : lovAttributeList) {
				boolean isFound = false;
				int index = 0;
				for (ChargeCodePendingModel charge : chargePendingList) {
					if (log.isDebugEnabled()) {
						log.debug("charge.getProductCode()=" + charge.getProductCode());
						log.debug("charge.getChargeCode()=" + charge.getChargeCode());
						log.debug("chargeCode.getChargeCode()=" + chargeCode.getChargeCode());
					}

					if (charge.getProductCode().equals(baseProductCode)
							&& charge.getChargeCode().equals(chargeCode.getChargeCode())) {
						isFound = true;
						charge.setLovAttribute(chargeCode.getLovAttribute());
						charge.setLovAttributeDesc(prodChargeBean.getLovAttrDescription(chargeCode.getLovAttribute()));
						charge.setLovId(chargeCode.getLovId());
						charge.setLovDesc(prodChargeBean.getLovValueDescription(chargeCode.getLovId()));
						chargePendingList.set(index, charge);
						break;
					}
					index++;
				}

				if (log.isDebugEnabled()) {
					log.debug("existing record exist?" + isFound);
				}
				if (!isFound) {
					chargeCode.setProductCode(baseProductCode);
					chargeCode.setLovAttributeDesc(prodChargeBean.getLovAttrDescription(chargeCode.getLovAttribute()));
					chargeCode.setLovDesc(prodChargeBean.getLovValueDescription(chargeCode.getLovId()));
					chargePendingList.add(chargeCode);
				}

			}

			if (tempList != null) {
				if (log.isDebugEnabled()) {
					log.debug("size of tempList:" + (tempList == null ? 0 : tempList.size()));
				}

				for (ChargeCodePendingModel chargeCode : tempList) {
					boolean isClone = false;

					if (cloneList != null) {
						if (log.isDebugEnabled()) {
							log.debug("size of cloneList:" + (cloneList == null ? 0 : cloneList.size()));
						}
						for (ChargeCodePendingModel clonedChargeCode : cloneList) {
							if (clonedChargeCode.getProductCode().equals(baseProductCode)
									&& clonedChargeCode.getChargeCode().equals(chargeCode.getChargeCode())) {
								isClone = true;
								break;
							}
						}
					}

					if (log.isDebugEnabled()) {
						log.debug("isClone:" + isClone);
					}
					if (!isClone) {
						chargePendingList.add(chargeCode);
					}

				}
			}
		} else {
			chargePendingList = new ArrayList<ChargeCodePendingModel>();
		}
		if (log.isDebugEnabled()) {
			log.debug("chargeList.size()  == " + chargeList.size());
			log.debug("chargePendingList.size()  == " + chargePendingList.size());
		}
	}

	private void getChargeMethodDefintionTable(ProductDefinitionBean formBean) throws Exception {
		MethodBean methBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("fgetChargeMethodDefintionTable -- start ");
			// US-GER Product Group Sanjeevi
			// log.debug("formBean.getSelectedChargeOnList() " +
			// formBean.getSelectedChargeOnList());
			/*
			 * for (String a : formBean.getSelectedChargeOnList()) { log.debug("a   == " +
			 * a); }
			 */
		}
		List<ChargeMethodPendingModel> chargePendingList = new ArrayList<ChargeMethodPendingModel>();
		// get from main product
		List<ChargeMethodModel> chargeList = productDefinitionService.getChargeMethodList(formBean.getCountryCode(),
				formBean.getInterfaceName(), formBean.getProductCode(), "");
		putChargeList(chargeList, chargePendingList);
		if (log.isDebugEnabled()) {
			log.debug("chargePendingList  == " + chargePendingList.size());
			for (ChargeMethodPendingModel model : chargePendingList) {
				log.debug("getChargeCode  == " + model.getChargeCode());
				log.debug("getComputationMethod  == " + model.getComputationMethod());
			}
		}
		// get from sub product code
		if (formBean.getLovProductsArray() != null) {
			if (log.isDebugEnabled()) {
				log.debug("formBean.getLovProductsArray().length=" + formBean.getLovProductsArray().length);
			}
			for (int i = 0; i < formBean.getLovProductsArray().length; i++) {
				SubProductVO prodVC = (SubProductVO) formBean.getLovProductsArray()[i];
				if (log.isDebugEnabled()) {
					log.debug("prodVC.getSubProductCode()  == " + prodVC.getSubProductCode());
				}
				if (prodVC.getSubProductCode() != null) {
					chargeList = new ArrayList<ChargeMethodModel>();
					chargeList = productDefinitionService.getChargeMethodList(formBean.getCountryCode(),
							formBean.getInterfaceName(), prodVC.getSubProductCode(), "");
					putChargeList(chargeList, chargePendingList);
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("chargePendingList  == " + chargePendingList.size());
			for (ChargeMethodPendingModel model : chargePendingList) {
				log.debug("getChargeCode  == " + model.getChargeCode());
				log.debug("getComputationMethod  == " + model.getComputationMethod());
			}
		}
		// put it to charge methodd table List
		if (chargePendingList != null) {
			methBean.setSubMethodList(chargePendingList);
			methBean.setDefChargeMethodList(chargePendingList);
		}
		methBean.setCountryCode(formBean.getCountryCode());
		methBean.setInterfaceName(formBean.getInterfaceName());
		// methBean.setProductCodeList();
	}

	private void putChargeList(List<ChargeMethodModel> chargeList, List<ChargeMethodPendingModel> chargePendingList)
			throws Exception {
		MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		if (chargeList != null && chargeList.size() > 0) {
			for (ChargeMethodModel chargeMethodModel : chargeList) {
				ChargeMethodPendingModel pendingModel = new ChargeMethodPendingModel();
				BeanUtils.copyProperties(chargeMethodModel, pendingModel);
				pendingModel.setChargeOn1Desc(methodBean.getChargeOnDescription(pendingModel.getChargeOn1()));
				pendingModel.setChargeOn2Desc(methodBean.getChargeOnDescription(pendingModel.getChargeOn2()));
				pendingModel.setChargeOn3Desc(methodBean.getChargeOnDescription(pendingModel.getChargeOn3()));
				pendingModel.setBaseVersion(chargeMethodModel.getCurrentVersion());
				if (ChargeMethod.isSingleTier(pendingModel.getComputationMethod())
						|| ChargeMethod.isFixedFlat(pendingModel.getComputationMethod())
						|| ChargeMethod.isBicBased(pendingModel.getComputationMethod())) {
					pendingModel.setApplyChargeFlag(pendingModel.getChargeOn1AggrOpt());
				} else {
					pendingModel.setApplyChargeFlag(pendingModel.getChargeOn2AggrOpt());

				}
				chargePendingList.add(pendingModel);
			}
		} else {
			chargeList = new ArrayList<ChargeMethodModel>();
		}
		if (log.isDebugEnabled()) {
			log.debug("chargeList.size()  == " + chargeList.size());
			log.debug("chargePendingList.size()  == " + chargePendingList.size());
		}
	}

	private void setPopulatePrimaryKeyFields() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		SelectItem[] iFace = new SelectItem[1];
		iFace[0] = new SelectItem(formBean.getInterfaceName(), formBean.getInterfaceName());
		// formBean.setInterfaceSelectItem(iFace);

	}

	private HashMap<String, ProductLovModel> putLovListFromSearch(List<ProductLovModel> lovList,
			ProductDefinitionBean formBean) {
		HashMap<String, ProductLovModel> defaultLovMap = new HashMap<String, ProductLovModel>();
		List<String> tempList = new ArrayList<String>();
		for (ProductLovModel model : lovList) {
			tempList.add(model.getLovAttributeId().toString());
			defaultLovMap.put(model.getLovAttributeId().toString(), model);
			if (log.isDebugEnabled()) {
				log.debug("productChargeOnModel.getChargeOnAttributeId().toString() = ["
						+ model.getLovAttributeId().toString() + "]");

			}
		}
		formBean.setDefaultLovMap(defaultLovMap);
		if (formBean.getPendingId() == null || formBean.getPendingId() == 0) {
			formBean.setSelectedLOVAttributesList(tempList);
			formBean.setDefSelectedLOVAttributesList(tempList);
			formBean.setBkSelectedLOVAttributesList(tempList);
		}
		formBean.setDefaultLovMap(defaultLovMap);
		if (log.isDebugEnabled()) {
			log.debug("formBean.getPendingId() = [" + formBean.getPendingId() + "]");
			// log.debug("formBean.getSelectedLOVAttributesList().size() = ["+
			// formBean.getSelectedLOVAttributesList().size()+ "]");
		}
		return defaultLovMap;
	}

	private HashMap<String, ProductChargeOnModel> putChargeListFromSearch(List<ProductChargeOnModel> chargeList,
			ProductDefinitionBean formBean) {
		// US-GER Product Group Sanjeevi start
		// List<String> selectedChargeOnList = new ArrayList<String>();
		List<AvailableCharrgeOnBean> selectedChargeOnList = new ArrayList<AvailableCharrgeOnBean>();
		// US-GER Product Group Sanjeevi End
		HashMap<String, ProductChargeOnModel> defaultChargeMap = new HashMap<String, ProductChargeOnModel>();
		for (ProductChargeOnModel productChargeOnModel : chargeList) {
			if (log.isDebugEnabled()) {
				log.debug("productChargeOnModel.getChargeOnAttributeId().toString() = ["
						+ productChargeOnModel.getChargeOnAttributeId().toString() + "]");
			}
			// US-GER Product Group Sanjeevi start
			// selectedChargeOnList.add(productChargeOnModel.getChargeOnAttributeId().toString());
			selectedChargeOnList.add(new AvailableCharrgeOnBean(productChargeOnModel.getChargeOnAttributeId(),
					productChargeOnModel.getAttribute().getAttributeType(),
					productChargeOnModel.getAttribute().getDescription()));
			// US-GER Product Group Sanjeevi end
			defaultChargeMap.put(productChargeOnModel.getChargeOnAttributeId().toString(), productChargeOnModel);
		}
		if (formBean.getPendingId() == null || formBean.getPendingId() == 0) {
			formBean.setSelectedChargeOnList(selectedChargeOnList);
			formBean.setDefSelectedChargeOnList(selectedChargeOnList);
			formBean.setBkSelectedChargeOnList(selectedChargeOnList);
		}
		formBean.setDefaultChargeMap(defaultChargeMap);
		if (log.isDebugEnabled()) {
			log.debug("formBean.getPendingId() = [" + formBean.getPendingId() + "]");
			// US-GER Product Group Sanjeevi
			/*
			 * log .debug("formBean.getSelectedChargeOnList().size() = [" +
			 * formBean.getSelectedChargeOnList().size() + "]");
			 */
		}
		return defaultChargeMap;
	}

	/**
	 * Clears the search criteria(s).
	 * 
	 * @return the toViewId; null if go back to the same page.
	 */
	public String doSearchClear() {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		formBean.clearSearch();
		if (log.isDebugEnabled()) {
			log.debug("start:  formBean = " + formBean);
		}
		return null;
	}

	public String doReset() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		MethodBean methBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		PandLSetupBean pandLSetupBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		RuleBean ruleForm = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("start:  formBean = " + formBean);
		}
		if (formBean.getMaintenanceStep().equals(MaintenanceStep.New.value())) {
			ruleForm.setMaintenanceStep(MaintenanceStep.New.value());
			/* ruleForm.resetRuleButton(); */
			ruleForm.resetRuleDetails();
			ruleForm.setListValues();
			ruleForm.setStatus(BaseConstants.Status.UNSAVE.value());

			// added by srikanth for multilang us&ge
			chargeBean.clearMethodDetails();
			// end
			chargeBean.clearPartnerMethodDetails();
			chargeBean.resetChargeCodeDetails();
			chargeBean.setStatus(BaseConstants.Status.UNSAVE.value());
			chargeBean.setMaintenanceStep(MaintenanceStep.New.value());

			methBean.resetMethodDetails();
			methBean.setStatus(BaseConstants.Status.UNSAVE.value());
			methBean.setMaintenanceStep(MaintenanceStep.New.value());

			pandLSetupBean.resetPLAccountDetails();
			// pandLSetupBean.clearPLAccountDetails();
			pandLSetupBean.setStatus(BaseConstants.Status.UNSAVE.value());
			pandLSetupBean.setMaintenanceStep(MaintenanceStep.New.value());

			this.doNew();
			formBean.setStatus(BaseConstants.Status.UNSAVE.value());
		} else if (formBean.getPendingId() != null && formBean.getPendingId() > 0) {
			ProductPendingModel productPending = productDefinitionService
					.getProductByPendingId(formBean.getPendingId());
			if (productPending != null) {
				// BeanUtils.copyProperties(productPending, formBean); //remarked by
				// ectan@20090612 - standardize the
				// code (HK sit defect [4])

				// start added by ectan@20090612 - standardize the code (HK sit defect [4])
				formBean.setBaseProductFlag(productPending.getBaseProductFlag());
				formBean.setBranchCode(productPending.getBranchCode());
				formBean.setChargeRebateFlag(productPending.getChargeRebateFlag());
				formBean.setCheckerDate(productPending.getCheckerDate());
				formBean.setCheckerId(productPending.getCheckerId());
				formBean.setClientTimeZone(productPending.getClientTimeZone());
				formBean.setCountryCode(productPending.getCountryCode());
				formBean.setCreatedDate(productPending.getCreatedDate());
				formBean.setDescription(productPending.getDescription());
				formBean.setHierarchyValue(productPending.getHierarchyValue());
				formBean.setInterfaceName(productPending.getInterfaceName());
				formBean.setInvDescLocalLang(productPending.getInvDescLocalLang());
				formBean.setInvoiceDescription(productPending.getInvoiceDescription());
				formBean.setLowestEntityLevel(productPending.getLowestEntityLevel());
				formBean.setLowestFrequency(productPending.getLowestFrequency());
				formBean.setMainProduct(productPending.getMainProduct());
				formBean.setMakerDate(productPending.getMakerDate());
				formBean.setMakerId(productPending.getMakerId());
				formBean.setParentProductCode(productPending.getParentProductCode());
				formBean.setPrecedence(productPending.getPrecedence());
				formBean.setProductCategory(productPending.getProductCategory());
				formBean.setProductCode(productPending.getProductCode());
				formBean.setProductLevel(productPending.getProductLevel());
				formBean.setProductType(productPending.getProductType());
				formBean.setRuleId(productPending.getRuleId());
				formBean.setStatus(productPending.getStatus());
				// Non Gpbs changes By Ramadevi T
				formBean.setNonGpbsPrdType(productPending.getNonGpbsPrdType());
				formBean.setNonGpbsRemarks(productPending.getNonGpbsRemarks());
				formBean.setPenaltyType(productPending.getPenaltyCtg());

				// Added by Sudhakar J for Transaction Grouping
				formBean.setTxnProductGrouping(productPending.getTxnProductGrouping());

				if (productPending.getProductDerivePriority() != null
						&& StringUtils.hasLength(productPending.getProductDerivePriority().toString())) {
					formBean.setProductDerivePriority(productPending.getProductDerivePriority().toString());
					if (productPending.getProductLevel().equalsIgnoreCase(ProductLevel.TXN.value()))
						formBean.setDuplicateFlag(productDefinitionService.checkProductDerivePriorityIfAvailable(
								productPending.getCountryCode(), ProductLevel.TXN.value(),
								productPending.getInterfaceName(),
								Integer.parseInt(productPending.getProductDerivePriority().toString())));
				}
				if (productPending.getUnitCost() != null) {
					formBean.setUnitCost(productPending.getUnitCost().toPlainString());
				}
				// for #cr112 by cheah
				if (productPending.getRepetitiveLimit() != null) {
					formBean.setRepetitiveLimit(productPending.getRepetitiveLimit().toPlainString());
				}
				formBean.setUpdatedDate(productPending.getUpdatedDate());
				formBean.setWbdwProductCode(productPending.getWbdwProductCode());
				formBean.setWbdwProductCodeDesc(productPending.getWbdwProductCodeDesc());
				formBean.setPendingId(productPending.getPendingId());
				formBean.setParentProductCode(productPending.getParentProductCode());
				// end added by ectan@20090612 - standardize the code (HK sit defect [4])
				// Start - DPS
				formBean.setDpsProductCode(productPending.getDpsProductCode());
				formBean.setDpsProductType(productPending.getDpsProductType());
				// end - DPS
				// Start - CR#16
				formBean.setShowChargeOnAsVolumeFlag(productPending.getIsShowChargeOnAsVol());
				formBean.setVolumeChargeOnAttribute(productPending.getVolChargeOnAttrId());
				// end - CR#16
				// Start - CR#VolProduct_Derivation_GroupBy
				formBean.setGroupByAttrId(productPending.getGroupByAttrId());
				formBean.setTxnNarrative1(productPending.getTxnNarrative1());
				// End - CR#VolProduct_Derivation_GroupBy
				// Start - CR#Volume Tiered Pricing
				formBean.setTxnRollUpFlag(productPending.getTxnRollUpFlag());
				// End - CR#Volume Tiered Pricing

				// Added for charge on pass through flag.
				formBean.setChargePassThroughFlag(productPending.getChargePassThroughFlag());

				formBean.setGlobalRebateFlag(productPending.getGlobalRebateFlag());
				// MY FPX - reversal change
				formBean.setTaxCreditFlag(productPending.getTaxCreditFlag());

				formBean.setOnlineFlag(productPending.getOnlineFlag());
				formBean.setScpayProduct(productPending.getScpayProduct());
				formBean.setIsEnabledOnlineBilling(productDefinitionService
						.getOnlineBillingFlag(productPending.getCountryCode(), productPending.getInterfaceName()));

				this.getPendingDetails(formBean, productPending.getPendingId().toString());
			}
			formBean.setMaintenanceStep(MaintenanceStep.Modify.value());

		} else if (StringUtils.hasLength(formBean.getCountryCode())
				&& StringUtils.hasLength(formBean.getInterfaceName())
				&& StringUtils.hasLength(formBean.getProductCode())) {
			ProductModelPK prodPK = new ProductModelPK();
			prodPK.setCountryCode(formBean.getCountryCode());
			// prodPK.setInterfaceName(formBean.getInterfaceName());
			prodPK.setProductCode(formBean.getProductCode());
			ProductModel prod = productDefinitionService.getProduct(prodPK);
			if (prod != null) {
				// BeanUtils.copyProperties(prod, formBean); //remarked by ectan@20090612 -
				// standardized the code (hk
				// sit defect [4])

				// start added by ectan@20090612 - standardized the code (hk sit defect [4])
				formBean.setBaseProductFlag(prod.getBaseProductFlag());
				formBean.setBranchCode(prod.getBranchCode());
				formBean.setChargeRebateFlag(prod.getChargeRebateFlag());
				formBean.setCheckerDate(prod.getCheckerDate());
				formBean.setCheckerId(prod.getCheckerId());
				formBean.setClientTimeZone(prod.getClientTimeZone());
				formBean.setCountryCode(prod.getCountryCode());
				formBean.setCreatedDate(prod.getCreatedDate());
				formBean.setDescription(prod.getDescription());
				formBean.setHierarchyValue(prod.getHierarchyValue());
				formBean.setInterfaceName(prod.getInterfaceName());
				formBean.setInvDescLocalLang(prod.getInvDescLocalLang());
				formBean.setInvoiceDescription(prod.getInvoiceDescription());
				formBean.setLowestEntityLevel(prod.getLowestEntityLevel());
				formBean.setLowestFrequency(prod.getLowestFrequency());
				formBean.setMainProduct(prod.getMainProduct());
				formBean.setMakerDate(prod.getMakerDate());
				formBean.setMakerId(prod.getMakerId());
				formBean.setParentProductCode(prod.getParentProductCode());
				formBean.setPrecedence(prod.getPrecedence());
				formBean.setProductCategory(prod.getProductCategory());
				formBean.setProductCode(prod.getProductCode());
				formBean.setProductLevel(prod.getProductLevel());
				formBean.setProductType(prod.getProductType());
				formBean.setRuleId(prod.getRuleId());
				formBean.setStatus(prod.getStatus());
				formBean.setOnlineFlag(prod.getOnlineFlag());
				formBean.setScpayProduct(prod.getScpayProduct());
				formBean.setIsEnabledOnlineBilling(
						productDefinitionService.getOnlineBillingFlag(prod.getCountryCode(), prod.getInterfaceName()));
				// Non Gpbs changes by ramadevi T
				formBean.setNonGpbsPrdType(prod.getNonGpbsPrdType());
				formBean.setNonGpbsRemarks(prod.getNonGpbsRemarks());
				formBean.setPenaltyType(prod.getPenaltyCtg());

				// Added by Sudhakar J for Transaction Grouping
				formBean.setTxnProductGrouping(prod.getTxnProductGrouping());

				if (prod.getProductDerivePriority() != null
						&& StringUtils.hasLength(prod.getProductDerivePriority().toString())) {
					formBean.setProductDerivePriority(prod.getProductDerivePriority().toString());
					if (prod.getProductLevel().equalsIgnoreCase(ProductLevel.TXN.value()))
						formBean.setDuplicateFlag(productDefinitionService.checkProductDerivePriorityIfAvailable(
								prod.getCountryCode(), ProductLevel.TXN.value(), prod.getInterfaceName(),
								Integer.parseInt(prod.getProductDerivePriority().toString())));
				}
				if (prod.getUnitCost() != null) {
					formBean.setUnitCost(prod.getUnitCost().toPlainString());
				}
				// for #cr112 by cheah
				if (prod.getRepetitiveLimit() != null) {
					formBean.setRepetitiveLimit(prod.getRepetitiveLimit().toPlainString());
				}

				formBean.setUpdatedDate(prod.getUpdatedDate());
				formBean.setWbdwProductCode(prod.getWbdwProductCode());
				formBean.setWbdwProductCodeDesc(prod.getWbdwProductCodeDesc());
				// end added by ectan@20090612 - standardized the code (hk sit defect [4])
				// Start - DPS
				formBean.setDpsProductCode(prod.getDpsProductCode());
				formBean.setDpsProductType(prod.getDpsProductType());
				// end - DPS
				// Start - CR#16
				formBean.setShowChargeOnAsVolumeFlag(prod.getIsShowChargeOnAsVol());
				formBean.setVolumeChargeOnAttribute(prod.getVolChargeOnAttrId());
				// end - CR#16
				// Start - CR#VolProduct_Derivation_GroupBy
				formBean.setGroupByAttrId(prod.getGroupByAttrId());
				formBean.setTxnNarrative1(prod.getTxnNarrative1());
				// End - CR#VolProduct_Derivation_GroupBy
				// Start - CR#Volume Tiered Pricing
				formBean.setTxnRollUpFlag(prod.getTxnRollUpFlag());
				// End - CR#Volume Tiered Pricing
				// Added for charge on pass through flag.
				formBean.setChargePassThroughFlag(prod.getChargePassThroughFlag());

				formBean.setGlobalRebateFlag(prod.getGlobalRebateFlag());
				// MY FPX - reversal change
				formBean.setTaxCreditFlag(prod.getTaxCreditFlag());

				formBean.setMaintenanceStep(MaintenanceStep.Modify.value());
				this.getMasterDetails(formBean);
			}
		} else {
			formBean.reset();
			formBean.setMaintenanceStep(MaintenanceStep.New.value());
		}
		if (log.isDebugEnabled()) {
			log.debug("end:  formBean = " + formBean);
		}
		return null;
	}

	private void getRules(List<RulesPendingModel> rulesList) {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		try {
			RuleBean ruleBean = (RuleBean) super.getManagedBean(RuleBean.BACKING_BEAN_NAME);
			RuleActionBean ruleActionBean = (RuleActionBean) super.getManagedBean(RuleActionBean.BACKING_BEAN_NAME);
			// To set the values into Rules tab.
			ruleBean.setCountryCode(formBean.getCountryCode());
			ruleBean.setInterfaceName(formBean.getInterfaceName());
			ruleBean.setProductCode(formBean.getProductCode());
			ruleBean.setProductDesc(formBean.getDescription());

			ruleActionBean.readRules(rulesList);
			/*
			 * ruleBean.setSubRulesList( rulesList ); ruleBean.setDefRulesList( rulesList );
			 * if(rulesList!=null){ if ( log.isDebugEnabled() ) { log.debug("rulesList - " +
			 * rulesList.toString()); } if(rulesList.size()==1 &&
			 * rulesList.get(0).getRuleType()!=null ){ ruleBean.setDisplaySubRulesList( null
			 * ); if ( rulesList.get(0).getRuleType ().equals(RuleCategory.EXPR.toString())
			 * ) { ruleBean.setExpressionFlag( true ); ruleBean.setFunctionFlag( false );
			 * ruleBean.setRuleType( RuleCategory.EXPR.toString() ); } else if (
			 * rulesList.get(0).getRuleType().equals(RuleCategory.FUNCT .toString()) ) {
			 * ruleBean.setFunctionFlag( true ); ruleBean.setExpressionFlag( false );
			 * ruleBean.setRuleType( RuleCategory.FUNCT.toString() ); }else{
			 * ruleBean.setDisplaySubRulesList( rulesList ); ruleBean.setExpressionFlag(
			 * false ); ruleBean.setFunctionFlag( false ); ruleBean.setRuleType(
			 * RuleCategory.BASIC.toString() ); } }else{ int status = 0; int index = 0;
			 * List<RulesPendingModel> deletedList = new ArrayList<RulesPendingModel>();
			 * List<RulesPendingModel> newList = new ArrayList<RulesPendingModel>(); for (
			 * RulesPendingModel pendingModel: rulesList ) { status =
			 * pendingModel.getStatus(); if ( status == Status.DELETE_DRAFT.value() ||
			 * status == Status.DELETE_REJECT.value() || status ==
			 * Status.DELETE_SUBMIT.value() ) { log.debug( "To be moved to Deleted list" );
			 * deletedList.add( pendingModel ); } else { newList.add( pendingModel ); }
			 * index++; } ruleBean.setDeletedSubRulesList( deletedList );
			 * ruleBean.setSubRulesList( newList ); ruleBean.setDisplaySubRulesList( newList
			 * ); ruleBean.setExpressionFlag( false ); ruleBean.setFunctionFlag( false );
			 * ruleBean.setRuleType( RuleCategory.BASIC.toString() ); } } else {
			 * ruleBean.setDisplaySubRulesList( null ); } ruleBean.updateDerivationRule();
			 */

			if (formBean.getProductLevel() != null
					&& formBean.getProductLevel().equals(ProductLevel.CROSSBORDER.value())) {
				ruleBean.setIsCrossBorderCrossProduct(true);
				/* ruleBean.setRuleCountryList(); */
			} else if (formBean.getProductLevel() != null
					&& formBean.getProductLevel().equals(ProductLevel.CROSSPRODUCT.value())) {
				ruleBean.setIsCrossProduct(true);
				/* ruleBean.setRuleProductList( formBean.getCountryCode() ); */
			} else {
				ruleBean.setIsCrossProduct(false);
				ruleBean.setIsCrossBorderCrossProduct(false);
				ruleBean.setAttributeList();
			}

		} catch (Exception ex) {
			log.error("Fail to retrieve rule:" + formBean.getRuleId() + " with exception: " + ex);
		}
	}

	private void putPendingProductChargeList(List<ProductChargeOnPendingModel> chargeList,
			ProductDefinitionBean formBean) {
		// US-GER Product Group Sanjeevi start
		// List<String> selectedPendingChargeOn = new ArrayList<String>();
		List<AvailableCharrgeOnBean> selectedPendingChargeOn = new ArrayList<AvailableCharrgeOnBean>();
		// US-GER Product Group Sanjeevi end
		// HashMap<String, ProductChargeOnPendingModel> pendingChargeMap = new
		// HashMap<String, ProductChargeOnPendingModel>();
		if (chargeList != null && !chargeList.isEmpty()) {
			for (ProductChargeOnPendingModel model : chargeList) {
				if (log.isDebugEnabled()) {
					log.debug("getAttributeId  = " + model.getChargeOnAttributeId());
					// log.debug("getDescription = " +
					// model.getAttribute().getDescription());
				}
				if (model.getStatus() != BaseConstants.Status.DELETE_DRAFT.value()
						&& model.getStatus() != BaseConstants.Status.DELETE_SUBMIT.value()) {
					// US-GER Product Group Sanjeevi start
					// selectedPendingChargeOn.add(model.getChargeOnAttributeId().toString());
					selectedPendingChargeOn.add(new AvailableCharrgeOnBean(model.getChargeOnAttributeId(),
							model.getAttribute().getAttributeType(), model.getAttribute().getDescription()));
					// US-GER Product Group Sanjeevi end
				}
				// pendingChargeMap.put(model.getChargeOnAttributeId().toString()+model.getProductCode(),
				// model);
			}
		}
		formBean.setDefSelectedChargeOnList(selectedPendingChargeOn);
		formBean.setSelectedChargeOnList(selectedPendingChargeOn);
		// formBean.setPendingChargeMap(pendingChargeMap);
	}

	private void putPendingChargeMap(List<ProductChargeOnPendingModel> chargeList, ProductDefinitionBean formBean) {
		// List<String> selectedPendingChargeOn = new ArrayList<String>();
		HashMap<String, ProductChargeOnPendingModel> pendingChargeMap = new HashMap<String, ProductChargeOnPendingModel>();
		if (chargeList != null && !chargeList.isEmpty()) {
			for (ProductChargeOnPendingModel model : chargeList) {
				if (log.isDebugEnabled()) {
					log.debug("getAttributeId  = " + model.getChargeOnAttributeId());
					log.debug("model.getChargeOnAttributeId().toString()+model.getProductCode()  = "
							+ model.getChargeOnAttributeId().toString() + model.getProductCode());
					// log.debug("getDescription = " +
					// model.getAttribute().getDescription());
				}
				// selectedPendingChargeOn.add(model.getChargeOnAttributeId().toString());
				pendingChargeMap.put(model.getChargeOnAttributeId().toString() + model.getProductCode(), model);
			}
		}
		/*
		 * formBean.setDefSelectedChargeOnList(selectedPendingChargeOn);
		 * formBean.setSelectedChargeOnList(selectedPendingChargeOn);
		 */
		formBean.setPendingChargeMap(pendingChargeMap);
		if (log.isDebugEnabled()) {
			log.debug("formBean.getPendingChargeMap().size()  = " + formBean.getPendingChargeMap().size());
		}
	}

	private void putPendingProductLovList(List<ProductLovPendingModel> lovList, ProductDefinitionBean formBean) {
		List<String> selectedPendingLov = new ArrayList<String>();
		// List<LovAttributeBean> lovAttList = new
		// ArrayList<LovAttributeBean>();
		// LovAttributeBean lovBean = null;
		List<ProductLovModel> modelList = new ArrayList<ProductLovModel>();
		HashMap<String, ProductLovPendingModel> pendingLovMap = new HashMap<String, ProductLovPendingModel>();
		if (lovList != null && !lovList.isEmpty()) {
			ProductLovModel prodLov = null;
			for (ProductLovPendingModel model : lovList) {
				if (log.isDebugEnabled()) {
					log.debug("getAttributeId  = " + model.getLovAttributeId());
					// log.debug("getDescription = " + ( model.getFile() ==
					// null? "":model.getFile().getDescription()));
					log.debug("getStatus  = " + model.getStatus());
				}
				if (model.getStatus() != BaseConstants.Status.DELETE_DRAFT.value()
						|| model.getStatus() != BaseConstants.Status.DELETE_SUBMIT.value()) {
					prodLov = new ProductLovModel();
					BeanUtils.copyProperties(model, prodLov);
					modelList.add(prodLov);
					selectedPendingLov.add(model.getLovAttributeId().toString());

					pendingLovMap.put(model.getLovAttributeId().toString(), model);
				}
				/*
				 * lovBean = new LovAttributeBean();
				 * lovBean.setAttributeId(model.getLovAttributeId().toString());
				 * lovBean.setAttributeLabel(model.getFile() == null? "":
				 * model.getFile().getDescription());
				 * lovBean.setAttributeVersion(model.getCurrentVersion());
				 * lovBean.setStatus(model.getStatus());
				 * lovBean.setLovLevel(model.getLovLevel()); lovAttList.add(lovBean);
				 */
			}
		}
		formBean.setProductLovList(modelList);
		formBean.setDefSelectedLOVAttributesList(selectedPendingLov);
		formBean.setSelectedLOVAttributesList(selectedPendingLov);
		formBean.setPendingLovMap(pendingLovMap);
		// formBean.setDefSelectedLovAttribute(lovAttList);
		// formBean.setSelectedLovAttribute(lovAttList);
	}

	private void getPendingChargeCodeTable(ProductDefinitionBean formBean) throws Exception {
		ProductChargeBean chargeBean = (ProductChargeBean) super.getManagedBean(ProductChargeBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("formBean.getLovProductsArray().length  == " + formBean.getLovProductsArray().length);
		}
		List<ChargeCodePendingModel> chargePendingList = new ArrayList<ChargeCodePendingModel>();
		List<ChargeCodePendingModel> deletedList = new ArrayList<ChargeCodePendingModel>();
		// get from main product
		// List<ChargeCodeModel> chargeList =
		// productDefinitionService.getChangeCodeList(formBean.getCountryCode(),
		// formBean.getInterfaceName(), formBean.getProductCode());
		List<ChargeCodePendingModel> chargeList = productDefinitionService
				.getChargeCodePendingByParentId(formBean.getPendingId());
		/*
		 * putPendingChargeCodeList(chargeList,
		 * chargePendingList,formBean.getProductCode());
		 */
		List<ChargeCodePendingModel> allChargeList = new ArrayList<ChargeCodePendingModel>();
		allChargeList.addAll(chargeList);

		// get from sub product code
		if (formBean.getLovProductsArray() != null) {
			for (int i = 0; i < formBean.getLovProductsArray().length; i++) {
				SubProductVO prodVC = (SubProductVO) formBean.getLovProductsArray()[i];
				if (log.isDebugEnabled()) {
					log.debug("prodVC.getSubProductCode()  == " + prodVC.getSubProductCode());
					log.debug("prodVC.getPendingId()  == " + prodVC.getPendingId());
				}
				chargeList = new ArrayList<ChargeCodePendingModel>();
				chargeList = productDefinitionService.getChargeCodePendingByParentId(prodVC.getPendingId());
				allChargeList.addAll(chargeList);
				/*
				 * putPendingChargeCodeList(chargeList, chargePendingList,
				 * prodVC.getSubProductCode());
				 */
			}
		}

		putPendingChargeCodeList(allChargeList, chargePendingList, formBean.getProductCode(), deletedList);

		// put it to charge code table List
		if (chargePendingList != null) {
			chargeBean.setChargeCodeList(chargePendingList);
			chargeBean.setChargeCodeDeletedList(deletedList);
			chargeBean.setDefChargeCodeList(chargePendingList);
		}
		if (log.isDebugEnabled()) {
			log.debug("formBean.getCountryCode()  == " + formBean.getCountryCode());
			log.debug("formBean.getInterfaceName()  == " + formBean.getInterfaceName());
		}
		chargeBean.setCountryCode(formBean.getCountryCode());
		chargeBean.setInterfaceName(formBean.getInterfaceName());
	}

	private void putPendingChargeCodeList(List<ChargeCodePendingModel> chargeList,
			List<ChargeCodePendingModel> chargePendingList, String baseProductCode,
			List<ChargeCodePendingModel> deletedList) throws Exception {
		ProductChargeBean prodChargeBean = (ProductChargeBean) super.getManagedBean(
				ProductChargeBean.BACKING_BEAN_NAME);

		if (chargeList != null) {
			// ChargeCodePendingModel pendingModel = null;
			List<ChargeCodePendingModel> lovAttributeList = new ArrayList<ChargeCodePendingModel>();
			List<ChargeCodePendingModel> cloneList = new ArrayList<ChargeCodePendingModel>();
			List<ChargeCodePendingModel> tempList = new ArrayList<ChargeCodePendingModel>();
			if (log.isDebugEnabled()) {
				log.debug("size of chargeList:" + (chargeList == null ? 0 : chargeList.size()));
			}
			for (ChargeCodePendingModel pendingModel : chargeList) {
				// pendingModel = new ChargeCodePendingModel();
				// BeanUtils.copyProperties(model, pendingModel);
				if (log.isDebugEnabled()) {
					log.debug("pendingModel.getLovAttribute():" + pendingModel.getLovAttribute());
					log.debug("pendingModel.getLovId():" + pendingModel.getLovId());
				}
				if ((pendingModel.getLovAttribute() != null)
						&& (!pendingModel.getLovAttribute().equals(BaseConstants.ZERO))) {

					if (lovAttributeList == null || lovAttributeList.size() == 0) {
						lovAttributeList.add(pendingModel);
					} else {
						boolean isFound = false;
						for (ChargeCodePendingModel tempChargeCode : lovAttributeList) {
							if (tempChargeCode.getLovAttribute().equals(pendingModel.getLovAttribute())
									&& tempChargeCode.getLovId().equals(pendingModel.getLovId())) {
								isFound = true;
								break;
							}

						}
						if (log.isDebugEnabled()) {
							log.debug("existing record with same lov exist?" + isFound);
						}
						if (!isFound) {
							lovAttributeList.add(pendingModel);
						}
					}

				}
				// DO not display the cloned charge codes from base product.
				else if (!baseProductCode.equals(pendingModel.getProductCode())) {
					tempList.add(pendingModel);
				} else if (baseProductCode.equals(pendingModel.getProductCode())) {
					cloneList.add(pendingModel);

					/*
					 * if (pendingModel.getStatus() != null && (pendingModel.getStatus() ==
					 * Status.DELETE_DRAFT .value() || pendingModel.getStatus() ==
					 * Status.DELETE_REJECT .value() || pendingModel .getStatus() ==
					 * Status.DELETE_SUBMIT .value())) { deletedList.add(pendingModel); } else {
					 */
					chargePendingList.add(pendingModel);
					/* } */

				} else {

					/*
					 * if (pendingModel.getStatus() != null && (pendingModel.getStatus() ==
					 * Status.DELETE_DRAFT .value() || pendingModel.getStatus() ==
					 * Status.DELETE_REJECT .value() || pendingModel .getStatus() ==
					 * Status.DELETE_SUBMIT .value())) { deletedList.add(pendingModel); } else {
					 */
					chargePendingList.add(pendingModel);
					/* } */
				}

			}

			// When display, if there is any LOV attribute tied to the charge
			// code,
			// display the record as base product with LOV attribute and values.
			if (log.isDebugEnabled()) {
				log.debug("size of lovAttributeList:" + (lovAttributeList == null ? 0 : lovAttributeList.size()));
			}
			for (ChargeCodePendingModel chargeCode : lovAttributeList) {
				boolean isFound = false;
				int index = 0;
				if (log.isDebugEnabled()) {
					log.debug(
							"size of chargePendingList:" + (chargePendingList == null ? 0 : chargePendingList.size()));
				}
				for (ChargeCodePendingModel charge : chargePendingList) {
					if (log.isDebugEnabled()) {
						log.debug("charge.getProductCode()=" + charge.getProductCode());
						log.debug("charge.getChargeCode()=" + charge.getChargeCode());
						log.debug("chargeCode.getChargeCode()=" + chargeCode.getChargeCode());
					}

					if (charge.getProductCode().equals(baseProductCode)
							&& charge.getChargeCode().equals(chargeCode.getChargeCode())) {
						isFound = true;
						charge.setLovAttribute(chargeCode.getLovAttribute());
						charge.setLovAttributeDesc(prodChargeBean.getLovAttrDescription(chargeCode.getLovAttribute()));
						charge.setLovId(chargeCode.getLovId());
						charge.setLovDesc(prodChargeBean.getLovValueDescription(chargeCode.getLovId(),
								chargeCode.getLovAttribute()));
						chargePendingList.set(index, charge);
						/*
						 * if (charge.getStatus() != null && (charge.getStatus() == Status.DELETE_DRAFT
						 * .value() || charge.getStatus() == Status.DELETE_REJECT .value() ||
						 * charge.getStatus() == Status.DELETE_SUBMIT .value())) {
						 * deletedList.add(charge); } else { chargePendingList.set(index, charge); }
						 */
						break;
					}
					index++;
				}

				if (log.isDebugEnabled()) {
					log.debug("existing record exist?" + isFound);
				}
				if (!isFound) {
					chargeCode.setProductCode(baseProductCode);
					chargeCode.setLovAttributeDesc(prodChargeBean.getLovAttrDescription(chargeCode.getLovAttribute()));
					chargeCode.setLovDesc(
							prodChargeBean.getLovValueDescription(chargeCode.getLovId(), chargeCode.getLovAttribute()));
					chargePendingList.add(chargeCode);
					/*
					 * if (chargeCode.getStatus() != null && (chargeCode.getStatus() ==
					 * Status.DELETE_DRAFT .value() || chargeCode.getStatus() ==
					 * Status.DELETE_REJECT .value() || chargeCode.getStatus() ==
					 * Status.DELETE_SUBMIT .value())) { deletedList.add(chargeCode); } else {
					 * chargePendingList.add(chargeCode); }
					 */
				}

			}

			if (tempList != null) {
				for (ChargeCodePendingModel chargeCode : tempList) {
					boolean isClone = false;

					if (cloneList != null) {
						for (ChargeCodePendingModel clonedChargeCode : cloneList) {
							if (clonedChargeCode.getProductCode().equals(baseProductCode)
									&& clonedChargeCode.getChargeCode().equals(chargeCode.getChargeCode())) {
								isClone = true;
								break;
							}
						}
					}

					if (!isClone) {
						/*
						 * if (chargeCode.getStatus() != null && (chargeCode.getStatus() ==
						 * Status.DELETE_DRAFT .value() || chargeCode.getStatus() ==
						 * Status.DELETE_REJECT .value() || chargeCode .getStatus() ==
						 * Status.DELETE_SUBMIT .value())) { deletedList.add(chargeCode); } else {
						 * chargePendingList.add(chargeCode); }
						 */
						chargePendingList.add(chargeCode);
					}

				}
			}

		} else {
			chargeList = new ArrayList<ChargeCodePendingModel>();
		}
		if (log.isDebugEnabled()) {
			log.debug("chargeList.size()  == " + chargeList.size());
			log.debug("chargePendingList.size()  == " + chargePendingList.size());
		}
	}

	private void getPendingChargeMethodDefintionTable(ProductDefinitionBean formBean) throws Exception {
		MethodBean methBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("formBean.getLovProductsArray().length  == " + formBean.getLovProductsArray().length);
		}
		List<ChargeMethodPendingModel> chargePendingList = new ArrayList<ChargeMethodPendingModel>();
		List<ChargeMethodPendingModel> deletedList = new ArrayList<ChargeMethodPendingModel>();
		// get from main product
		List<ChargeMethodPendingModel> chargeList = productDefinitionService
				.getChargeMethodPendingByParentId(formBean.getPendingId());
		putPendingChargeList(chargeList, chargePendingList, deletedList, methBean);

		// get from sub product code
		if (formBean.getLovProductsArray() != null) {
			for (int i = 0; i < formBean.getLovProductsArray().length; i++) {
				SubProductVO prodVC = (SubProductVO) formBean.getLovProductsArray()[i];
				if (log.isDebugEnabled()) {
					log.debug("prodVC.getSubProductCode()  == " + prodVC.getSubProductCode());
				}
				chargeList = new ArrayList<ChargeMethodPendingModel>();
				chargeList = productDefinitionService.getChargeMethodPendingByParentId(prodVC.getPendingId());
				putPendingChargeList(chargeList, chargePendingList, deletedList, methBean);
			}
		}

		// put it to charge methodd table List
		if (chargePendingList != null) {
			methBean.setSubMethodList(new ArrayList<ChargeMethodPendingModel>());
			methBean.setSubMethodList(chargePendingList);
			methBean.setDeleteMethodList(deletedList);
			methBean.setDefChargeMethodList(chargePendingList);
		}

		if (log.isDebugEnabled()) {
			log.debug("formBean.getCountryCode()  == " + formBean.getCountryCode());
			log.debug("formBean.getInterfaceName()  == " + formBean.getInterfaceName());
		}
		methBean.setCountryCode(formBean.getCountryCode());
		methBean.setInterfaceName(formBean.getInterfaceName());
	}

	private void putPendingChargeList(List<ChargeMethodPendingModel> chargeList,
			List<ChargeMethodPendingModel> chargePendingList, List<ChargeMethodPendingModel> deletedList,
			MethodBean methodBean) throws Exception {
		if (chargeList != null) {
			int status = 0;
			for (ChargeMethodPendingModel pendingModel : chargeList) {
				ChargeMethodPendingModel newPendingModel = new ChargeMethodPendingModel();
				BeanUtils.copyProperties(pendingModel, newPendingModel);
				newPendingModel.setChargeOn1Desc(methodBean.getChargeOnDescription(newPendingModel.getChargeOn1()));
				newPendingModel.setChargeOn2Desc(methodBean.getChargeOnDescription(newPendingModel.getChargeOn2()));
				// Added for BANCA by Parvathi 1535919
				if (methodBean.getChargeOnDescription(newPendingModel.getChargeOn3()) != null) {
					newPendingModel.setChargeOn3Desc(methodBean.getChargeOnDescription(newPendingModel.getChargeOn3()));
				}
				if (ChargeMethod.isSingleTier(newPendingModel.getComputationMethod())
						|| ChargeMethod.isFixedFlat(newPendingModel.getComputationMethod())
						|| ChargeMethod.isBicBased(newPendingModel.getComputationMethod())) {
					newPendingModel.setApplyChargeFlag(newPendingModel.getChargeOn1AggrOpt());
				} else {
					newPendingModel.setApplyChargeFlag(newPendingModel.getChargeOn2AggrOpt());

				}
				/*
				 * status = newPendingModel.getStatus(); if (status ==
				 * Status.DELETE_DRAFT.value() || status == Status.DELETE_REJECT.value() ||
				 * status == Status.DELETE_SUBMIT.value()) { if (log.isDebugEnabled())
				 * log.debug("To be moved to Deleted list"); deletedList.add(newPendingModel); }
				 * else { chargePendingList.add(newPendingModel); }
				 */
				chargePendingList.add(newPendingModel);
			}
		} else {
			chargeList = new ArrayList<ChargeMethodPendingModel>();
		}
		if (log.isDebugEnabled()) {
			log.debug("chargeList.size()  == " + chargeList.size());
			log.debug("chargePendingList.size()  == " + chargePendingList.size());
		}
	}

	private void getPendingPLAccountDefintionTable(ProductDefinitionBean formBean) throws Exception {
		PandLSetupBean panlBean = (PandLSetupBean) super.getManagedBean(PandLSetupBean.BACKING_BEAN_NAME);
		if (log.isDebugEnabled()) {
			log.debug("formBean.getLovProductsArray().length  == " + formBean.getLovProductsArray().length);
		}
		List<ProductPLAccountPendingModel> plAccountPendingList = new ArrayList<ProductPLAccountPendingModel>();
		List<ProductPLAccountPendingModel> deletedList = new ArrayList<ProductPLAccountPendingModel>();
		// get from main product
		List<ProductPLAccountPendingModel> plAccountList = productDefinitionService
				.getProductPLAccountPendingByParentId(formBean.getPendingId());
		putPendingPLAccountList(plAccountList, plAccountPendingList, deletedList, panlBean);

		// get from sub product code
		if (formBean.getLovProductsArray() != null) {
			for (int i = 0; i < formBean.getLovProductsArray().length; i++) {
				SubProductVO prodVC = (SubProductVO) formBean.getLovProductsArray()[i];
				if (log.isDebugEnabled()) {
					log.debug("prodVC.getSubProductCode()  == " + prodVC.getSubProductCode());
				}
				plAccountList = new ArrayList<ProductPLAccountPendingModel>();
				plAccountList = productDefinitionService.getProductPLAccountPendingByParentId(prodVC.getPendingId());
				putPendingPLAccountList(plAccountList, plAccountPendingList, deletedList, panlBean);
			}
		}

		// put it to charge methodd table List
		if (plAccountPendingList != null) {
			panlBean.setSubPLAccountList(new ArrayList<ProductPLAccountPendingModel>());
			panlBean.setSubPLAccountList(plAccountPendingList);
			BeanUtils.copyProperties(deletedList, plAccountPendingList);
			panlBean.setDeletePLAccountList(deletedList);
			panlBean.setDefPLAccountList(plAccountPendingList);
		}

		if (log.isDebugEnabled()) {
			log.debug("formBean.getCountryCode()  == " + formBean.getCountryCode());
			log.debug("formBean.getInterfaceName()  == " + formBean.getInterfaceName());
		}
		panlBean.setCountryCode(formBean.getCountryCode());
		panlBean.setInterfaceName(formBean.getInterfaceName());
	}

	private void putPendingPLAccountList(List<ProductPLAccountPendingModel> plAccountList,
			List<ProductPLAccountPendingModel> plAccountPendingList, List<ProductPLAccountPendingModel> deletedList,
			PandLSetupBean methodBean) throws Exception {
		if (plAccountList != null) {
			int status = 0;
			for (ProductPLAccountPendingModel pendingModel : plAccountList) {
				ProductPLAccountPendingModel newPendingModel = new ProductPLAccountPendingModel();
				BeanUtils.copyProperties(pendingModel, newPendingModel);
				plAccountPendingList.add(newPendingModel);
			}
		} else {
			plAccountList = new ArrayList<ProductPLAccountPendingModel>();
		}
		if (log.isDebugEnabled()) {
			log.debug("plAccountList.size()  == " + plAccountList.size());
			log.debug("plAccountPendingList.size()  == " + plAccountPendingList.size());
		}
	}

	private boolean validateProductCode(String countryCode, String interfaceName, String productCode) throws Exception {
		ProductModelPK pk = new ProductModelPK();
		pk.setCountryCode(countryCode);
		// pk.setInterfaceName(interfaceName);
		pk.setProductCode(productCode);
		// change to validate product code is only country specific exculding
		// interface
		// ProductModel product = productDefinitionService.getProduct(pk);
		ProductModel product = productDefinitionService.getProductIgnoreInterface(pk.getCountryCode(),
				pk.getProductCode(), false);
		if (product != null) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_PRODUCT_LABEL_DUPLICATE,
					new Object[] { pk.getProductCode() });
			return false;
		}

		return true;
	}

	/**
	 * Cascade delete PSGL_MAPPING, BILLING_PRODUCT and TIERING.
	 * 
	 * @param checkerMakerVO
	 * @param originalStatus
	 * @param countryCode
	 * @throws Exception
	 */
	private void cascadeDelete(CheckerMakerVO checkerMakerVO, Status originalStatus, String countryCode)
			throws Exception {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Start cascadeDelete --------------------------");
			}
			List<BillingProductModel> deleteSubmitList = getDeleteSubmitList(checkerMakerVO);
			if (deleteSubmitList != null && !deleteSubmitList.isEmpty()) {
				this.productDefinitionService.deletePsglAndBillingProductAndTiering(countryCode, deleteSubmitList,
						originalStatus);
			}
		} catch (Exception e) {
			FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_DELETEALLRELATEDRECORDS_FAILED,
					null);
		}
	}

	private List<BillingProductModel> getDeleteSubmitList(CheckerMakerVO checkerMakerVO) throws Exception {

		if (checkerMakerVO == null) {
			throw new Exception("Missing checkerMakerVO");
		}
		if (checkerMakerVO.getParent() == null) {
			throw new Exception("Missing checkerMakerVO.parent");
		}
		if (!checkerMakerVO.getParent().getClass().equals(ProductPendingModel.class)) {
			throw new Exception("Invalid checkerMakerVO.parent = " + checkerMakerVO.getParent().getClass());
		}

		ProductPendingModel productPending = (ProductPendingModel) checkerMakerVO.getParent();

		List<BillingProductModel> changedList = new ArrayList<BillingProductModel>();
		int i = 0;

		if (log.isDebugEnabled()) {
			log.debug("ProductPending.getStatus() - " + productPending.getStatus());
		}

		// if the parent model (Product) is deleted
		if (productPending != null && productPending.getStatus().equals(BaseConstants.Status.DELETE_SUBMIT.value())) {
			BillingProductModel bp = new BillingProductModel();
			bp.setProductCode(productPending.getProductCode());
			if (!changedList.contains(bp)) {
				changedList.add(bp);
				if (log.isDebugEnabled()) {
					log.debug(i + ": added ProductPendingModel to changedList = " + bp);
				}
				i++;
			}
		} else if (checkerMakerVO != null && checkerMakerVO.getChildList() != null) {

			for (BasePAModel child : checkerMakerVO.getChildList()) {
				if (child.getClass().equals(ProductPLAccountPendingModel.class)) {
					ProductPLAccountPendingModel pandlpending = (ProductPLAccountPendingModel) child;
					if (pandlpending.getStatus().equals(BaseConstants.Status.DELETE_SUBMIT.value())) {
						BillingProductModel bp = new BillingProductModel();
						bp.setProductCode(pandlpending.getProductCode());
						bp.setChargeCode(pandlpending.getChargeCode());

						if (!changedList.contains(bp)) {
							changedList.add(bp);
							if (log.isDebugEnabled()) {
								log.debug(i + ": added ChargeMethodPendingModel to changedList = " + bp);
							}
							i++;
						}
					}
				} else if (child.getClass().equals(ChargeMethodPendingModel.class)) {
					ChargeMethodPendingModel pending = (ChargeMethodPendingModel) child;
					if (pending.getStatus().equals(BaseConstants.Status.DELETE_SUBMIT.value())) {
						BillingProductModel bp = new BillingProductModel();
						bp.setProductCode(pending.getProductCode());
						bp.setChargeCode(pending.getChargeCode());
						bp.setComputationMethod(pending.getComputationMethod());
						if (!changedList.contains(bp)) {
							changedList.add(bp);
							if (log.isDebugEnabled()) {
								log.debug(i + ": added ChargeMethodPendingModel to changedList = " + bp);
							}
							i++;
						}
					}
				} else if (child.getClass().equals(ChargeCodePendingModel.class)) {
					ChargeCodePendingModel pending = (ChargeCodePendingModel) child;
					if (pending.getStatus().equals(BaseConstants.Status.DELETE_SUBMIT.value())) {
						BillingProductModel bp = new BillingProductModel();
						bp.setProductCode(pending.getProductCode());
						bp.setChargeCode(pending.getChargeCode());
						if (!changedList.contains(bp)) {
							changedList.add(bp);
							if (log.isDebugEnabled()) {
								log.debug(i + ": added ChargeCodePendingModel to changedList = " + bp);
							}
							i++;
						}
					}
				}
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("return: size = " + (changedList != null ? changedList.size() : 0));
		}
		return changedList;
	}

	public String doInvDescInsertGroupBy() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		try {
			String txnInvoiceDescription = "";
			String groupByAttrNm = "<" + formBean.getGroupByAttrNm().trim() + ">";
			if (StringUtils.hasLength(formBean.getInvoiceDescription())) {
				if (!formBean.getInvoiceDescription().trim().contains(StringUtils.trimWhitespace(groupByAttrNm))) {
					txnInvoiceDescription = formBean.getInvoiceDescription() + groupByAttrNm;
				} else {
					txnInvoiceDescription = formBean.getInvoiceDescription();
				}
			} else {
				txnInvoiceDescription = groupByAttrNm;
			}
			formBean.setInvoiceDescription(txnInvoiceDescription);

		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	public String doInvDescLocalLangInsertGroupBy() throws Exception {
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		try {
			String txnInvDescLocalLang = "";
			String groupByAttrNm = "<" + formBean.getGroupByAttrNm().trim() + ">";
			if (StringUtils.hasLength(formBean.getInvDescLocalLang())) {
				if (!formBean.getInvDescLocalLang().trim().contains(StringUtils.trimWhitespace(groupByAttrNm))) {
					txnInvDescLocalLang = formBean.getInvDescLocalLang() + groupByAttrNm;
				} else {
					txnInvDescLocalLang = formBean.getInvDescLocalLang();
				}
			} else {
				txnInvDescLocalLang = groupByAttrNm;
			}
			formBean.setInvDescLocalLang(txnInvDescLocalLang);

		} catch (Exception e) {
			log.error(e);
		}
		return null;
	}

	/**
	 * Method to set charge pass through flag based on product level and product
	 * type
	 * 
	 * @param formBean      ProductDefinitionBean
	 * @param parentPending ProductPendingModel
	 * 
	 */
	private void setChargePassThroughFlag(ProductPendingModel parentPending, ProductDefinitionBean formBean) {
		parentPending.setChargePassThroughFlag(formBean.getChargePassThroughFlag());
		if (log.isDebugEnabled()) {
			log.debug(" setChargePassThroFlag method : value: " + parentPending.getChargePassThroughFlag());
		}

	}

	private void setGlobalRebateFlag(ProductPendingModel parentPending, ProductDefinitionBean formBean) {
		parentPending.setGlobalRebateFlag(formBean.getGlobalRebateFlag());
		if (log.isDebugEnabled()) {
			log.debug(" setGlobalRebateFlag method : value: " + parentPending.getGlobalRebateFlag());
		}
	}

	// MY FPX - reversal change
	private void setReversalFlag(ProductPendingModel parentPending, ProductDefinitionBean formBean) {
		parentPending.setTaxCreditFlag(formBean.getTaxCreditFlag());
		if (log.isDebugEnabled()) {
			log.debug(" setTaxCreditFlag method : value: " + parentPending.getTaxCreditFlag());
		}
	}

	// Added by srikanth for 4.4.1 for changes status - start
	private void setProdInvLangDescChildVO(String action, ProductDefinitionBean formBean,
			List<BasePAModel> baseModelList, ProductPendingModel parentProduct) {
		// if not from pending
		// if(formBean.getPendingId()==0){
		List<ProductInvoiceOthLangDesPending> langInvnvoiceDescList = formBean.getSubMethodList();

		if (action.equals(ClientAction.DELETE.value())) {
			setOtherInvDescDetails(action, BaseConstants.ActionType.DELETE.value(), baseModelList, formBean,
					parentProduct);
		} else {
			if (langInvnvoiceDescList != null && langInvnvoiceDescList.size() > 0) {
				if (formBean.getPendingId() == null || formBean.getPendingId() == 0) {

					String actionType = BaseConstants.ActionType.UNCHANGE.value();
					if (formBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())) {
						actionType = BaseConstants.ActionType.NEW.value();
					}
					setOtherInvDescDetails(action, actionType, baseModelList, formBean, parentProduct);
				} else {
					if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())) {
						setOtherInvDescDetails(action, BaseConstants.ActionType.UNCHANGE.value(), baseModelList,
								formBean, parentProduct);
					}
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
	}

	private void setOtherInvDescDetails(String action, String actionType, List<BasePAModel> baseModelList,
			ProductDefinitionBean formBean, ProductPendingModel parentProduct) {
		ProductInvoiceOthLangDesPending model = null;
		List<ProductInvoiceOthLangDesPending> methodList = formBean.getSubMethodList();

		if (methodList != null && methodList.size() > 0) {
			for (ProductInvoiceOthLangDesPending chargeOnId : methodList) {
				model = new ProductInvoiceOthLangDesPending();

				if (action.equals(ClientAction.SAVE_AS_SUBMIT.value())
						|| actionType.equals(BaseConstants.ActionType.UNCHANGE.value())) {
					if (formBean.getPendingId() > 0) {
						model.setActionType(null);
					} else {
						model.setActionType(actionType);
					}
				} else {
					model.setActionType(actionType);
				}
				model.setMakerId(super.getCurrentUserId());
				model.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
				model.setCountryCode(parentProduct.getCountryCode());
				model.setInterfaceName(parentProduct.getInterfaceName());
				model.setProductCode(parentProduct.getProductCode());

				if (actionType.equals(BaseConstants.ActionType.DISCARD.value())
						|| actionType.equals(BaseConstants.ActionType.UNCHANGE.value())) {

				} else if (actionType.equals(BaseConstants.ActionType.DISCARD.value())) {
					// check from master table
					ProductInvoiceOthLangDesModel charge = new ProductInvoiceOthLangDesModel();
					model.setBaseVersion(charge.getCurrentVersion());
					model.setStatus(charge.getStatus());
					model.setActionType(BaseConstants.ActionType.DELETE.value());
				} else if (actionType.equals(BaseConstants.ActionType.NEW.value())) {
					model.setStatus(BaseConstants.Status.UNSAVE.value());
					model.setActionType(BaseConstants.ActionType.NEW.value());
				}

				baseModelList.add(model);

			}
		}
	}

	// to set the child multi invoice language code in Product screen
	@SuppressWarnings("unused")
	private boolean setProdDefOtherLangInvDescChildVO(ProductPendingModel pendingModel, String action,
			List<BasePAModel> baseModelList, String productCode, ProductDefinitionBean formBean) throws Exception {
		if (action.equals(ClientAction.DELETE.value())) {
			List<ProductInvoiceOthLangDesModel> othLangInvList = productDefinitionService
					.getProdDefOtherLangInvDescMasterList(pendingModel.getCountryCode(),
							pendingModel.getInterfaceName(), pendingModel.getProductCode());
			if (othLangInvList != null) {
				ProductInvoiceOthLangDesPending methodPending = null;
				for (ProductInvoiceOthLangDesModel chargeMethodModel : othLangInvList) {
					methodPending = new ProductInvoiceOthLangDesPending();
					BeanUtils.copyProperties(chargeMethodModel, methodPending);

					methodPending.setBaseVersion(chargeMethodModel.getCurrentVersion());
					methodPending.setActionType(BaseConstants.ActionType.DELETE.value());
					methodPending.setMakerId(super.getCurrentUserId());
					methodPending.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(methodPending);
				}
			}
		} else {

			if (!this.generateProdDefOtherLangInvDescVO(productCode, baseModelList, formBean)) {
				return false;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("baseModelList.size()    == " + baseModelList.size());
		}
		return true;
	}

	/**
	 * To generate the Other invoice langugae code value object.
	 * 
	 * @param productCode   the product code of the charge code
	 * @param baseModelList the list of the base models
	 * @return the flag to indicate if the langugage code desc are valid.
	 * 
	 */
	public boolean generateProdDefOtherLangInvDescVO(String productCode, List<BasePAModel> baseModelList,
			ProductDefinitionBean productBean) throws Exception {
		// MethodBean formBean = (MethodBean)
		// super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		List<ProductInvoiceOthLangDesPending> methodList = productBean.getSubMethodList();
		if (methodList != null) {
			for (ProductInvoiceOthLangDesPending model : methodList) {
				if (model.getProductCode().equals(productCode) && model.getLanguageCodeDesc() != null) {
					if (model.getActionType() != null
							&& model.getActionType().equals(BaseConstants.ActionType.NEW.value())) {

					} else {
						if (productBean.getMaintenanceStep().equals(MaintenanceStep.Modify.value())) {

							if ((model.getActionType() == null || model.getActionType().trim().length() == 0)
									&& (model.getStatus() == Status.ACTIVE.value()
											|| model.getStatus() == Status.UNSAVE.value()
											|| model.getStatus() == Status.UNCHANGE.value())) {
								model.setActionType(BaseConstants.ActionType.UNCHANGE.value());
							}

						}
					}
					if (log.isDebugEnabled()) {
						log.debug("model.getActionType()   = " + model.getActionType());
						log.debug("model.getStatus()   = " + model.getStatus());
						log.debug("model.getProductCode()   = " + model.getProductCode());
					}

					model.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					model.setMakerId(super.getCurrentUserId());
					model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(model);
				}
			}
		}

		List<ProductInvoiceOthLangDesPending> delMethodList = productBean.getDeleteMethodList();
		if (delMethodList != null) {
			if (log.isDebugEnabled()) {
				log.debug("delMethodList.size()=" + delMethodList.size());
			}
			for (ProductInvoiceOthLangDesPending model : delMethodList) {
				if (model.getProductCode().equals(productCode)) {
					model.setMakerDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					model.setMakerId(super.getCurrentUserId());
					model.setUpdatedDate(new java.sql.Timestamp(new java.util.Date().getTime()));
					baseModelList.add(model);
				}
			}
		}

		return true;
	}

	/**
	 * To get the quarterly Threshold Product count
	 * 
	 * @param countryCode
	 * @param productCode
	 * @param businessSegment
	 * @param lowestFrequency
	 * @return
	 * @throws Exception
	 */
	private boolean validateQuarterlyProductCode(String countryCode, String businessSegment, String productCode,
			String lowestFrequency) throws Exception {

		if (countryCode.equals(BaseConstants.COUNTRY_CODE_GLOBAL)
				&& businessSegment.equals(BaseConstants.TRADE_BUSINESS_SEGMENT)) {
			if (!lowestFrequency.equalsIgnoreCase("Q")) {

				int quarterlyProductCount = productDefinitionService.getQuarterlyThresholdProductCount(countryCode,
						productCode, businessSegment);
				if (quarterlyProductCount > 0) {
					return true;
				}
			}
		}

		return false;
	}

	public ProductChargeService getProductChargeService() {
		return productChargeService;
	}

	public void setProductChargeService(ProductChargeService productChargeService) {
		this.productChargeService = productChargeService;
	}

	private boolean validateBicBasedProduct() {
		boolean isValid = true;
		MethodBean methodBean = (MethodBean) super.getManagedBean(MethodBean.BACKING_BEAN_NAME);
		List<ChargeMethodPendingModel> subMethodList = methodBean.getSubMethodList();
		ProductDefinitionBean formBean = (ProductDefinitionBean) super.getManagedBean(
				ProductDefinitionBean.BACKING_BEAN_NAME);
		for (ChargeMethodPendingModel charge : subMethodList) {
			if (ChargeMethod.isBicBased(charge.getComputationMethod())
					&& (!BaseConstants.ProductLevel.TXN.value().equals(formBean.getProductLevel())
							|| !BaseConstants.ProductType.CHARGE.value().equals(formBean.getProductType()))) {
				FacesUtil.addErrorMessage(null, MessageConstants.PRODUCTDEFINITION_ERROR_VOLUMEBICNOTALLOWED);
			}
		}

		if (FacesUtil.getMessageCount() > 0) {
			isValid = false;
		}

		return isValid;
	}

	private boolean validateRule(Long parentPendingId) {
		return productDefinitionService.validateRuleExp(parentPendingId);
	}

}
