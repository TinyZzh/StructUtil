/*
 *
 *
 *          Copyright (c) 2020. - TinyZ.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.struct.core;

import org.struct.annotation.StructSheet;

/**
 * @author sxr
 */
@StructSheet(fileName = "tpl_weapon_type_level.xlsx", sheetName = "Data_道具表", startOrder = 3)
public class WeaponTypeLevelConfig {
    private int weaponType;
    private int num;
    private int raceType;
    private String challengeRuleKey;
    private int sceneId;

    private long researchFund1;
    private long crystal1;
    private long diamond1;
    private long pack1;

    private long researchFund2;
    private long crystal2;
    private long diamond2;
    private long pack2;

    private long researchFund3;
    private long crystal3;
    private long diamond3;
    private long pack3;

    public int getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(int weaponType) {
        this.weaponType = weaponType;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getRaceType() {
        return raceType;
    }

    public void setRaceType(int raceType) {
        this.raceType = raceType;
    }

    public String getChallengeRuleKey() {
        return challengeRuleKey;
    }

    public void setChallengeRuleKey(String challengeRuleKey) {
        this.challengeRuleKey = challengeRuleKey;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public long getResearchFund1() {
        return researchFund1;
    }

    public void setResearchFund1(long researchFund1) {
        this.researchFund1 = researchFund1;
    }

    public long getCrystal1() {
        return crystal1;
    }

    public void setCrystal1(long crystal1) {
        this.crystal1 = crystal1;
    }

    public long getDiamond1() {
        return diamond1;
    }

    public void setDiamond1(long diamond1) {
        this.diamond1 = diamond1;
    }

    public long getPack1() {
        return pack1;
    }

    public void setPack1(long pack1) {
        this.pack1 = pack1;
    }

    public long getResearchFund2() {
        return researchFund2;
    }

    public void setResearchFund2(long researchFund2) {
        this.researchFund2 = researchFund2;
    }

    public long getCrystal2() {
        return crystal2;
    }

    public void setCrystal2(long crystal2) {
        this.crystal2 = crystal2;
    }

    public long getDiamond2() {
        return diamond2;
    }

    public void setDiamond2(long diamond2) {
        this.diamond2 = diamond2;
    }

    public long getPack2() {
        return pack2;
    }

    public void setPack2(long pack2) {
        this.pack2 = pack2;
    }

    public long getResearchFund3() {
        return researchFund3;
    }

    public void setResearchFund3(long researchFund3) {
        this.researchFund3 = researchFund3;
    }

    public long getCrystal3() {
        return crystal3;
    }

    public void setCrystal3(long crystal3) {
        this.crystal3 = crystal3;
    }

    public long getDiamond3() {
        return diamond3;
    }

    public void setDiamond3(long diamond3) {
        this.diamond3 = diamond3;
    }

    public long getPack3() {
        return pack3;
    }

    public void setPack3(long pack3) {
        this.pack3 = pack3;
    }

    @Override
    public String toString() {
        return "WeaponTypeLevelConfig{" +
                "weaponType=" + weaponType +
                ", num=" + num +
                ", raceType=" + raceType +
                ", challengeRuleKey=" + challengeRuleKey +
                ", sceneId=" + sceneId +
                ", researchFund1=" + researchFund1 +
                ", crystal1=" + crystal1 +
                ", diamond1=" + diamond1 +
                ", pack1=" + pack1 +
                ", researchFund2=" + researchFund2 +
                ", crystal2=" + crystal2 +
                ", diamond2=" + diamond2 +
                ", pack2=" + pack2 +
                ", researchFund3=" + researchFund3 +
                ", crystal3=" + crystal3 +
                ", diamond3=" + diamond3 +
                ", pack3=" + pack3 +
                '}';
    }
}
