package arr.armuriii.spiritum.client;
import arr.armuriii.spiritum.Spiritum;
import arr.armuriii.spiritum.init.SpiritumEntities;
import arr.armuriii.spiritum.init.SpiritumPotions;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.*;
import net.minecraft.block.Block;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;


import static arr.armuriii.spiritum.init.SpiritumItems.*;
import static arr.armuriii.spiritum.init.SpiritumBlocks.*;

public class SpiritumDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		FabricDataGenerator.Pack pack = generator.createPack();
		pack.addProvider(ModelGenerator::new);
		pack.addProvider(LanguageGenerator::new);
		pack.addProvider(CraftingGenerator::new);
		pack.addProvider(LootGenerator::new);
		pack.addProvider(BlockTagGenerator::new);
	}

	private static class ModelGenerator extends FabricModelProvider {
		public ModelGenerator(FabricDataOutput output) {super(output);}
		@Override
		public void generateBlockStateModels(BlockStateModelGenerator generator) {
			generator.registerCubeAllModelTexturePool(FLESH_BLOCK);
			generator.registerCubeAllModelTexturePool(SILVER_BLOCK);
			generator.registerCubeAllModelTexturePool(POLISHED_SILVER_BLOCK);
			generator.registerCubeAllModelTexturePool(HEXSTONE);
			generator.registerCubeAllModelTexturePool(SILVERED_HEXSTONE);
			generator.registerCubeAllModelTexturePool(HEXSTONE_BRICKS);
			generator.registerCubeAllModelTexturePool(POLISHED_HEXSTONE);

			registerAllVariants(generator,List.of(HEXSTONE_STAIRS,HEXSTONE_SLAB,HEXSTONE_WALL),Spiritum.id("block/hexstone"));
			registerAllVariants(generator,List.of(POLISHED_HEXSTONE_STAIRS,POLISHED_HEXSTONE_SLAB,POLISHED_HEXSTONE_WALL),Spiritum.id("block/hexstone"));
			registerAllVariants(generator,List.of(HEXSTONE_BRICKS_STAIRS,HEXSTONE_BRICKS_SLAB,HEXSTONE_BRICKS_WALL),Spiritum.id("block/hexstone"));

			registerAllVariants(generator,List.of(POLISHED_SILVER_STAIRS,POLISHED_SILVER_SLAB,POLISHED_SILVER_WALL),Spiritum.id("block/polished_silver_block"));
		}

		private void registerAllVariants(BlockStateModelGenerator generator, List<Block> blocks, Identifier path) {
			registerStairs(generator,blocks.get(0),path);
			registerSlab(generator,blocks.get(1),path);
			registerWall(generator,blocks.get(2),path);
		}

		private void registerStairs(BlockStateModelGenerator generator,Block block,Identifier path) {
			final TextureMap diamondTexture = TextureMap.all(path);
			final Identifier stairsModelId = Models.STAIRS.upload(block, diamondTexture, generator.modelCollector);
			final Identifier innerStairsModelId = Models.INNER_STAIRS.upload(block, diamondTexture, generator.modelCollector);
			final Identifier outerStairsModelId = Models.OUTER_STAIRS.upload(block, diamondTexture, generator.modelCollector);
			generator.blockStateCollector.accept(
					BlockStateModelGenerator.createStairsBlockState(block,
							innerStairsModelId,
							stairsModelId,
							outerStairsModelId));
			generator.registerParentedItemModel(block, stairsModelId);
		}

		private void registerSlab(BlockStateModelGenerator generator,Block block,Identifier path) {
			final TextureMap diamondTexture = TextureMap.all(path);
			final Identifier slabBottomModelId = Models.SLAB.upload(block, diamondTexture, generator.modelCollector);
			final Identifier slabTopModelId = Models.SLAB_TOP.upload(block, diamondTexture, generator.modelCollector);
			generator.blockStateCollector.accept(
					BlockStateModelGenerator.createSlabBlockState(block,
							slabBottomModelId,
							slabTopModelId,
							path)
			);
			generator.registerParentedItemModel(block, slabBottomModelId);
		}

		private void registerWall(BlockStateModelGenerator generator,Block block,Identifier path) {
			final TextureMap diamondTexture = TextureMap.all(path);
			final Identifier postModelId = Models.TEMPLATE_WALL_POST.upload(block, diamondTexture, generator.modelCollector);
			final Identifier lowSideModelId = Models.TEMPLATE_WALL_SIDE.upload(block, diamondTexture, generator.modelCollector);
			final Identifier tallSideModelId = Models.TEMPLATE_WALL_SIDE_TALL.upload(block, diamondTexture, generator.modelCollector);
			final Identifier itemModelId = Models.WALL_INVENTORY.upload(block, diamondTexture, generator.modelCollector);
			generator.blockStateCollector.accept(
					BlockStateModelGenerator.createWallBlockState(block,
							postModelId,
							lowSideModelId,
							tallSideModelId)
			);
			generator.registerParentedItemModel(block, itemModelId);
		}

		@Override
		public void generateItemModels(ItemModelGenerator generator) {
			generator.register(FLESH_CLUMP, Models.GENERATED);
			generator.register(POPPET, Models.GENERATED);
			generator.register(RAW_SILVER, Models.GENERATED);
			generator.register(SILVER_INGOT, Models.GENERATED);
			generator.register(SILVER_NUGGET, Models.GENERATED);
			generator.register(SILVER_NEEDLE, Models.GENERATED);
			generator.register(SPIRIT_BOTTLE, Models.GENERATED);
			//generator.register(SUMMONING_TOKEN, Models.GENERATED);
		}
	}

	private static class LanguageGenerator extends FabricLanguageProvider {
		public LanguageGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {super(dataOutput, registryLookup);}

		@Override
		public void generateTranslations(RegistryWrapper.WrapperLookup wrapper, TranslationBuilder builder) {
			builder.add(FLESH_CLUMP, "Flesh Clump");
			builder.add(POPPET, "Poppet");
			builder.add(RAW_SILVER, "Raw Silver");
			builder.add(SILVER_INGOT, "Silver Ingot");
			builder.add(SILVER_NUGGET, "Silver Nugget");
			builder.add(SILVER_NEEDLE, "Silver Needle");
			builder.add(TIPPED_SILVER_NEEDLE, "Tipped Silver Needle");
			builder.add(SPIRIT_BOTTLE, "Bottled Spirit");
			builder.add(SUMMONING_TOKEN, "Summoning Token");
			builder.add(FLESH_BLOCK,"Flesh Block");
			builder.add(SILVER_BLOCK,"Silver Block");
			builder.add(POLISHED_SILVER_BLOCK,"Polished Silver Block");
			builder.add(POLISHED_SILVER_STAIRS,"Polished Silver Block Stairs");
			builder.add(POLISHED_SILVER_SLAB,"Polished Silver Block Slab");
			builder.add(POLISHED_SILVER_WALL,"Polished Silver Block Wall");
			builder.add(HEXSTONE,"Hexstone");
			builder.add(HEXSTONE_STAIRS,"Hexstone Stairs");
			builder.add(HEXSTONE_SLAB,"Hexstone Slab");
			builder.add(HEXSTONE_WALL,"Hexstone Wall");
			builder.add(SILVERED_HEXSTONE,"Silvered Hexstone");
			builder.add(HEXSTONE_BRICKS,"Hexstone Bricks");
			builder.add(HEXSTONE_BRICKS_STAIRS,"Hexstone Bricks Stairs");
			builder.add(HEXSTONE_BRICKS_SLAB,"Hexstone Bricks Slab");
			builder.add(HEXSTONE_BRICKS_WALL,"Hexstone Bricks Wall");
			builder.add(POLISHED_HEXSTONE,"Polished Hexstone");
			builder.add(POLISHED_HEXSTONE_STAIRS,"Polished Hexstone Stairs");
			builder.add(POLISHED_HEXSTONE_SLAB,"Polished Hexstone Slab");
			builder.add(POLISHED_HEXSTONE_WALL,"Polished Hexstone Wall");
			builder.add(RITUAL_PEDESTAL,"Ritual Pedestal");

			/*builder.add(POLISHED_SILVER_STAIRS,"Polished Silver Stairs");
			builder.add(POLISHED_SILVER_SLAB,"Polished Silver Slab");
			builder.add(POLISHED_SILVER_WALL,"Polished Silver Wall");

			builder.add(HEXSTONE_STAIRS,"Hexstone Stairs");
			builder.add(HEXSTONE_SLAB,"Hexstone Slab");
			builder.add(HEXSTONE_WALL,"Hexstone Wall");

			builder.add(HEXSTONE_BRICKS_STAIRS,"Hexstone Bricks Stairs");
			builder.add(HEXSTONE_BRICKS_SLAB,"Hexstone Bricks Slab");
			builder.add(HEXSTONE_BRICKS_WALL,"Hexstone Bricks Wall");

			builder.add(POLISHED_HEXSTONE_STAIRS,"Polished Hexstone Stairs");
			builder.add(POLISHED_HEXSTONE_SLAB,"Polished Hexstone Slab");
			builder.add(POLISHED_HEXSTONE_WALL,"Polished Hexstone Wall");*/

			builder.add(SpiritumEntities.IMP,"Imp");
			builder.add(SpiritumPotions.LETHARGY.value().getTranslationKey(),"Lethargy");
			builder.add("item.minecraft.potion.effect.lethargy","Potion of Lethargy");
			builder.add("item.minecraft.splash_potion.effect.lethargy","Splash Potion of Lethargy");
			builder.add("item.minecraft.lingering_potion.effect.lethargy","Lingering Potion of Lethargy");

			builder.add("death.attack.hex","%1$s was hexed");
			builder.add("death.attack.hex.item","%1$s was hexed by %2$s using %3$s");
			builder.add("death.attack.hex.player","%1$s was hexed by %2$s");
			builder.add("itemGroup.spiritum.spiritum_group","Spiritum");
			builder.add("commands.ritual.success","%s ritual has : %s");
		}
	}

	private static class CraftingGenerator extends FabricRecipeProvider {
		public CraftingGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {super(output, registriesFuture);}

		@Override
		public void generate(RecipeExporter exporter) {
			RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,POLISHED_SILVER_BLOCK,SILVER_BLOCK);
			RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,POLISHED_HEXSTONE, HEXSTONE);
			RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,POLISHED_HEXSTONE, HEXSTONE_BRICKS);
			RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,HEXSTONE_BRICKS, HEXSTONE);
			RecipeProvider.offerStonecuttingRecipe(exporter, RecipeCategory.BUILDING_BLOCKS,HEXSTONE_BRICKS, POLISHED_HEXSTONE);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, POPPET)
					.pattern(" w ")
					.pattern("wfw")
					.pattern(" w ")
					.input('w', Items.WHEAT)
					.input('f', FLESH_CLUMP)
					.criterion(FabricRecipeProvider.hasItem(FLESH_CLUMP),
							FabricRecipeProvider.conditionsFromItem(FLESH_CLUMP))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, SILVER_NEEDLE,8)
					.pattern("n")
					.pattern("n")
					.pattern("i")
					.input('n', SILVER_NUGGET)
					.input('i', SILVER_INGOT)
					.criterion(FabricRecipeProvider.hasItem(SILVER_NUGGET),
							FabricRecipeProvider.conditionsFromItem(SILVER_NUGGET))
					.criterion(FabricRecipeProvider.hasItem(SILVER_INGOT),
							FabricRecipeProvider.conditionsFromItem(SILVER_INGOT))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, HEXSTONE_BRICKS,4)
					.pattern("hh")
					.pattern("hh")
					.input('h', HEXSTONE)
					.criterion(FabricRecipeProvider.hasItem(HEXSTONE),
							FabricRecipeProvider.conditionsFromItem(HEXSTONE))
					.offerTo(exporter);

			ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, RITUAL_PEDESTAL)
					.pattern("ss")
					.pattern("hh")
					.pattern("hh")
					.input('s', SILVER_INGOT)
					.input('h', HEXSTONE)
					.criterion(FabricRecipeProvider.hasItem(SILVER_INGOT),
							FabricRecipeProvider.conditionsFromItem(SILVER_INGOT))
					.offerTo(exporter);

			RecipeProvider.offerSmelting(exporter, List.of(HEXSTONE),RecipeCategory.MISC,POLISHED_HEXSTONE,2.0f,200,"hexstone");

			//RecipeProvider.offerSmelting(exporter, List.of(RAW_SILVER),RecipeCategory.MISC,SILVER_NUGGET,2.0f,300,"silver_nugget");
			RecipeProvider.offerSmelting(exporter, List.of(SILVERED_HEXSTONE),RecipeCategory.MISC,SILVER_INGOT,2.0f,300,"silver_ingot");

			//RecipeProvider.offerBlasting(exporter, List.of(RAW_SILVER),RecipeCategory.MISC,SILVER_NUGGET,2.0f,150,"silver_nugget");
			RecipeProvider.offerBlasting(exporter, List.of(SILVERED_HEXSTONE),RecipeCategory.MISC,SILVER_INGOT,2.0f,150,"silver_ingot");
		}
	}
	private static class LootGenerator extends FabricBlockLootTableProvider {

		protected LootGenerator(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(dataOutput, registryLookup);
		}

		@Override
		public void generate() {
			addDrop(SILVERED_HEXSTONE,oreDrops(SILVERED_HEXSTONE,RAW_SILVER));

			addDrop(FLESH_BLOCK);
			addDrop(HEXSTONE);
			addDrop(HEXSTONE_STAIRS);
			addDrop(HEXSTONE_SLAB);
			addDrop(HEXSTONE_WALL);

			addDrop(RITUAL_PEDESTAL);

			addDrop(HEXSTONE_BRICKS);
			addDrop(HEXSTONE_BRICKS_STAIRS);
			addDrop(HEXSTONE_BRICKS_SLAB);
			addDrop(HEXSTONE_BRICKS_WALL);

			addDrop(POLISHED_SILVER_BLOCK);
			addDrop(POLISHED_SILVER_STAIRS);
			addDrop(POLISHED_SILVER_SLAB);
			addDrop(POLISHED_SILVER_WALL);
			addDrop(SILVER_BLOCK);
			addDrop(POLISHED_HEXSTONE);
			addDrop(POLISHED_HEXSTONE_STAIRS);
			addDrop(POLISHED_HEXSTONE_SLAB);
			addDrop(POLISHED_HEXSTONE_WALL);
		}
	}
	private static class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {

		public BlockTagGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
			getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
					.add(HEXSTONE)
					.add(HEXSTONE_STAIRS)
					.add(HEXSTONE_SLAB)
					.add(HEXSTONE_WALL)
					.add(HEXSTONE_BRICKS)
					.add(HEXSTONE_BRICKS_STAIRS)
					.add(HEXSTONE_BRICKS_SLAB)
					.add(HEXSTONE_BRICKS_WALL)
					.add(POLISHED_HEXSTONE)
					.add(POLISHED_HEXSTONE_STAIRS)
					.add(POLISHED_HEXSTONE_SLAB)
					.add(POLISHED_HEXSTONE_WALL)
					.add(SILVERED_HEXSTONE)
					.add(SILVER_BLOCK)
					.add(POLISHED_SILVER_BLOCK)
					.add(POLISHED_SILVER_STAIRS)
					.add(POLISHED_SILVER_SLAB)
					.add(POLISHED_SILVER_WALL)
					.add(RITUAL_PEDESTAL)
					.setReplace(false);

			getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
					.add(HEXSTONE)
					.add(HEXSTONE_STAIRS)
					.add(HEXSTONE_SLAB)
					.add(HEXSTONE_WALL)
					.add(HEXSTONE_BRICKS)
					.add(HEXSTONE_BRICKS_STAIRS)
					.add(HEXSTONE_BRICKS_SLAB)
					.add(HEXSTONE_BRICKS_WALL)
					.add(POLISHED_HEXSTONE)
					.add(POLISHED_HEXSTONE_STAIRS)
					.add(POLISHED_HEXSTONE_SLAB)
					.add(POLISHED_HEXSTONE_WALL)
					.setReplace(false);

			getOrCreateTagBuilder(BlockTags.NEEDS_IRON_TOOL)
					.add(SILVERED_HEXSTONE)
					.add(SILVER_BLOCK)
					.add(POLISHED_SILVER_STAIRS)
					.add(POLISHED_SILVER_SLAB)
					.add(POLISHED_SILVER_WALL)
					.add(POLISHED_SILVER_BLOCK)
					.add(RITUAL_PEDESTAL)
					.setReplace(false);

			getOrCreateTagBuilder(BlockTags.WALLS)
					.add(HEXSTONE_BRICKS_WALL)
					.add(HEXSTONE_WALL)
					.add(POLISHED_SILVER_WALL)
					.add(POLISHED_HEXSTONE_WALL)
					.setReplace(false);
		}
	}
}
