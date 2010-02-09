package ruc.irm.similarity.word.hownet.sememe;

import java.util.ArrayList;
import java.util.List;

import ruc.irm.similarity.util.TraverseEvent;


/**
 * 实现遍历加载义原信息到义原表中, 义原词典的组织以知网导出的格式为标准，如：<br/> 
 * - entity|实体 <br/> 
 * ├ thing|万物 [#time|时间,#space|空间] <br/> 
 * │ ├ physical|物质 [!appearance|外观] <br/> 
 * │ │ ├ animate|生物 [*alive|活着,!age|年龄,*die|死,*metabolize|代谢] <br/> 
 * │ │ │ ├ AnimalHuman|动物 [!sex|性别,*AlterLocation|变空间位置,*StateMental|精神状态] <br/> 
 * │ │ │ │<br/>
 * 等等 <br>
 * 
 * <p>
 * Organization: Knowledge engeering laboratory, IRM, Renmin university of China
 * </p>
 * 
 * @author <a href="xiat@ruc.edu.cn">xiatian</a>
 * @version 1.0
 */
public class SememeDictTraverseEvent implements TraverseEvent<String>{
	/** 义原存放的列表, 按照顺序设置ID，存放到线性表中 */
	private List<Sememe> sememeList = null;
	
	public SememeDictTraverseEvent(){
		this.sememeList = new ArrayList<Sememe>();
	}
	
	/**
	 * 获取加载后的义原信息，按照下标顺序存放，树的层次关系通过数组下标表示
	 * @return
	 */
	public Sememe[] getSememes(){
		return sememeList.toArray(new Sememe[sememeList.size()]);
	}
	
	/**
	 * 解析当前义原信息文本行<br/>
	 * 判断读入的一行文本是义元树中的第几层，读入的格式形如：<br> - entity|实体 <br> ├ thing|万物
	 * [#time|时间,#space|空间] <br> │ ├ physical|物质 [!appearance|外观] <br> │ │ ├
	 * animate|生物 [*alive|活着,!age|年龄,*die|死,*metabolize|代谢] <br>
	 * 
	 * @param item
	 * @return 如果是义原，则info[0]返回层次深度(info[0]>=0); info[1]返回具体的义元内容起始位置；否则info[0]返回-1
	 */
	private int[] parseSememeLine(String item) {
		int[] info = new int[2];
		info[0] = -1;

		int prefixLen = 0; // 前缀的数目，包括空格和"-,│,├"等符号，其中空格和"-"符号算一个长度，其他算2个
		for (int i = 0; i < item.length(); i++) {
			char ch = item.charAt(i);
			if ((ch == ' ') || (ch == '-')) {
				prefixLen++;
			} else if ((ch == '├') || (ch == '│') || (ch == '└')) {
				prefixLen += 2;
			} else {
				// 遇到非前缀字符，求解，根据前缀深度，如果为2，返回0，即第一级，否则，每增加3，深度加1
				if (prefixLen >= 2) {
					info[0] = (prefixLen - 2) / 3;
					info[1] = i;
				}
				break;
			}
		}
		return info;
	}
	
	/**
	 * 根据字符串判断义元的类型
	 * 
	 * @param item
	 * @return
	 */
	private int parseSememeType(String item) {
		String myItem = item.toLowerCase().trim();
		if (myItem.indexOf("event|") == 0)
			return SememeType.Event;
		else if (myItem.indexOf("entity|") == 0)
			return SememeType.Entity;
		else if (myItem.indexOf("attribute|") == 0)
			return SememeType.Attribute;
		else if (myItem.indexOf("quantity|") == 0)
			return SememeType.Quantity;
		else if (myItem.indexOf("avalue|") == 0)
			return SememeType.AValue;
		else if (myItem.indexOf("qvalue|") == 0)
			return SememeType.QValue;
		else if (myItem.indexOf("secondary feature") == 0)
			return SememeType.SecondaryFeature;
		else if (myItem.indexOf("syntax") == 0)
			return SememeType.Syntax;
		else if (myItem.indexOf("eventrole and features") == 0)
			return SememeType.EventRoleAndFeature;
		else
			return 0;
	}
	
	/**
	 * 实现TraverseEvent<String>的实际访问接口, 返回值没有使用
	 * @see ke.commons.util.TraverseEvent
	 */
	public boolean visit(String line) {
		//判断是否为注释行
        if(line.trim().equals("")||line.trim().charAt(0)=='#') return true;
        
        //当前义原在整个义原列表中的位置
        int position = sememeList.size();
        
        //解析当前义原信息文本行, info[0]表示当前义原的层次, info[1]表示当前义原的实际信息在文本行中的开始位置
        int[] info = parseSememeLine(line);
        int curDepth = info[0];
        
        //如果深度<0，继续
        if(info[0]<0) return false;

        //取出真正的义原字符串
        String sememeString = line.substring(info[1]);
        
        //深度为0，表示为根节点
        if(info[0]==0){
        	Sememe sememe = new Sememe(position, position, 0, sememeString);
        	int sememeType = parseSememeType(sememeString);
        	sememe.setType(sememeType);
        	sememeList.add(sememe);
        }else{
        	Sememe parentSememe = sememeList.get(position-1);
        	//最近一个深度比当前深度大1的义原即为该义原的父节点
        	
        	while((parentSememe.getDepth()-curDepth)!=-1){
        		parentSememe = sememeList.get(parentSememe.getParentId());
        	}
        	Sememe sememe = new Sememe(position, parentSememe.getId(), curDepth, sememeString);
        	sememe.setType(parentSememe.getType());
        	sememeList.add(sememe);
        }
		
        return true;
	}
}